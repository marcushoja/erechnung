(() => {
    const tableBody = document.getElementById("items-body");
    const addButton = document.getElementById("add-row");
    const template = document.getElementById("item-row-template");
    const netTotalValue = document.getElementById("net-total-value");
    const grossTotalValue = document.getElementById("gross-total-value");
    const taxSummaryLines = document.getElementById("tax-summary-lines");

    if (!tableBody || !addButton || !template) {
        return;
    }

    const numberFormatter = new Intl.NumberFormat("de-DE", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

    const roundMoney = (value) => Math.round((value + Number.EPSILON) * 100) / 100;

    const parseDecimal = (value) => {
        if (!value) {
            return NaN;
        }
        return Number.parseFloat(value.replace(",", "."));
    };

    const vatRateFromValue = (value) => {
        switch (value) {
            case "VAT_7":
                return 7;
            case "VAT_0":
                return 0;
            case "VAT_19":
            default:
                return 19;
        }
    };

    const formatEuro = (amount) => `${numberFormatter.format(roundMoney(amount))} EUR`;

    const recalculateTotals = () => {
        if (!netTotalValue || !grossTotalValue || !taxSummaryLines) {
            return;
        }

        let netTotal = 0;
        const taxByRate = new Map();

        tableBody.querySelectorAll("tr.item-row").forEach((row) => {
            const quantity = parseDecimal(row.querySelector('input[name$=".quantity"]')?.value || "");
            const unitPrice = parseDecimal(row.querySelector('input[name$=".unitPriceNet"]')?.value || "");
            const vatRaw = row.querySelector('select[name$=".vatRate"]')?.value || "VAT_19";

            if (!Number.isFinite(quantity) || !Number.isFinite(unitPrice) || quantity <= 0 || unitPrice < 0) {
                return;
            }

            const lineNet = quantity * unitPrice;
            const vatRate = vatRateFromValue(vatRaw);
            const lineTax = lineNet * (vatRate / 100);

            netTotal += lineNet;
            taxByRate.set(vatRate, (taxByRate.get(vatRate) || 0) + lineTax);
        });

        let taxTotal = 0;
        const sortedRates = [...taxByRate.keys()].sort((a, b) => b - a);
        taxSummaryLines.innerHTML = "";
        sortedRates.forEach((rate) => {
            const amount = roundMoney(taxByRate.get(rate) || 0);
            taxTotal += amount;
            const line = document.createElement("p");
            line.innerHTML = `Steuer (${numberFormatter.format(rate)}%): <strong>${formatEuro(amount)}</strong>`;
            taxSummaryLines.append(line);
        });

        netTotal = roundMoney(netTotal);
        taxTotal = roundMoney(taxTotal);
        const grossTotal = roundMoney(netTotal + taxTotal);

        netTotalValue.textContent = formatEuro(netTotal);
        grossTotalValue.textContent = formatEuro(grossTotal);
    };

    const reindex = () => {
        const rows = tableBody.querySelectorAll("tr.item-row");
        rows.forEach((row, index) => {
            row.querySelectorAll("input, select").forEach((el) => {
                const name = el.getAttribute("name") || "";
                const id = el.getAttribute("id") || "";

                if (name.includes("items[")) {
                    el.setAttribute("name", name.replace(/items\[\d+]/, `items[${index}]`));
                }
                if (id.includes("items")) {
                    el.setAttribute("id", id.replace(/items\d+/, `items${index}`));
                }

                if (el.tagName === "INPUT" || el.tagName === "SELECT") {
                    el.addEventListener("input", recalculateTotals);
                    el.addEventListener("change", recalculateTotals);
                }
            });
        });
    };

    const removeHandler = (button) => {
        button.addEventListener("click", () => {
            const row = button.closest("tr.item-row");
            if (!row) {
                return;
            }
            if (tableBody.querySelectorAll("tr.item-row").length === 1) {
                return;
            }
            row.remove();
            reindex();
            recalculateTotals();
        });
    };

    tableBody.querySelectorAll(".remove-row").forEach(removeHandler);

    addButton.addEventListener("click", () => {
        const index = tableBody.querySelectorAll("tr.item-row").length;
        const html = template.innerHTML.replaceAll("__INDEX__", index.toString());
        tableBody.insertAdjacentHTML("beforeend", html);
        const newRow = tableBody.lastElementChild;
        const removeButton = newRow.querySelector(".remove-row");
        if (removeButton) {
            removeHandler(removeButton);
        }
        reindex();
        recalculateTotals();
    });

    reindex();
    recalculateTotals();
})();
