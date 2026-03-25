(() => {
    const tableBody = document.getElementById("items-body");
    const addButton = document.getElementById("add-row");
    const template = document.getElementById("item-row-template");
    const smallBusinessCheckbox = document.getElementById("small-business-checkbox");
    const netTotalValue = document.getElementById("net-total-value");
    const taxTotalValue = document.getElementById("tax-total-value");
    const taxTotalLine = document.getElementById("tax-total-line");
    const grossTotalLabel = document.getElementById("gross-total-label");
    const smallBusinessNote = document.getElementById("small-business-note");
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

    const isSmallBusiness = () => Boolean(smallBusinessCheckbox?.checked);

    const applySmallBusinessMode = () => {
        const enabled = isSmallBusiness();
        tableBody.querySelectorAll('select[name$=".vatRate"]').forEach((select) => {
            if (enabled) {
                select.value = "VAT_0";
            }
            select.classList.toggle("locked-vat", enabled);
        });
    };

    const recalculateTotals = () => {
        if (!netTotalValue || !grossTotalValue || !taxSummaryLines || !taxTotalValue) {
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
            const vatRate = isSmallBusiness() ? 0 : vatRateFromValue(vatRaw);
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
        taxTotalValue.textContent = formatEuro(taxTotal);
        grossTotalValue.textContent = formatEuro(grossTotal);

        if (isSmallBusiness()) {
            taxSummaryLines.innerHTML = "";
            taxTotalLine.style.display = "none";
            grossTotalLabel.textContent = "Gesamt:";
            if (smallBusinessNote) {
                smallBusinessNote.style.display = "block";
            }
        } else {
            taxTotalLine.style.display = "block";
            grossTotalLabel.textContent = "Gesamt (Brutto):";
            if (smallBusinessNote) {
                smallBusinessNote.style.display = "none";
            }
        }
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

    const copyHandler = (button) => {
        button.addEventListener("click", () => {
            const sourceRow = button.closest("tr.item-row");
            if (!sourceRow) {
                return;
            }

            const index = tableBody.querySelectorAll("tr.item-row").length;
            const html = template.innerHTML.replaceAll("__INDEX__", index.toString());
            tableBody.insertAdjacentHTML("beforeend", html);
            const newRow = tableBody.lastElementChild;

            const sourceQuantity = sourceRow.querySelector('input[name$=".quantity"]')?.value || "";
            const sourceDescription = sourceRow.querySelector('input[name$=".description"]')?.value || "";
            const sourceUnitPrice = sourceRow.querySelector('input[name$=".unitPriceNet"]')?.value || "";
            const sourceVat = sourceRow.querySelector('select[name$=".vatRate"]')?.value || "VAT_19";

            const quantityInput = newRow.querySelector('input[name$=".quantity"]');
            const descriptionInput = newRow.querySelector('input[name$=".description"]');
            const unitPriceInput = newRow.querySelector('input[name$=".unitPriceNet"]');
            const vatSelect = newRow.querySelector('select[name$=".vatRate"]');

            if (quantityInput) quantityInput.value = sourceQuantity;
            if (descriptionInput) descriptionInput.value = sourceDescription;
            if (unitPriceInput) unitPriceInput.value = sourceUnitPrice;
            if (vatSelect) vatSelect.value = isSmallBusiness() ? "VAT_0" : sourceVat;

            bindRow(newRow);
            reindex();
            applySmallBusinessMode();
            recalculateTotals();
        });
    };

    const bindRow = (row) => {
        row.querySelectorAll("input, select").forEach((el) => {
            if (el.dataset.bound === "true") {
                return;
            }
            el.addEventListener("input", recalculateTotals);
            el.addEventListener("change", recalculateTotals);
            el.dataset.bound = "true";
        });

        const removeButton = row.querySelector(".remove-row");
        if (removeButton && removeButton.dataset.bound !== "true") {
            removeHandler(removeButton);
            removeButton.dataset.bound = "true";
        }

        const copyButton = row.querySelector(".copy-row");
        if (copyButton && copyButton.dataset.bound !== "true") {
            copyHandler(copyButton);
            copyButton.dataset.bound = "true";
        }

        const vatSelect = row.querySelector('select[name$=".vatRate"]');
        if (vatSelect && vatSelect.dataset.lockHandlerBound !== "true") {
            vatSelect.addEventListener("change", () => {
                if (isSmallBusiness()) {
                    vatSelect.value = "VAT_0";
                    recalculateTotals();
                }
            });
            vatSelect.dataset.lockHandlerBound = "true";
        }
    };

    tableBody.querySelectorAll("tr.item-row").forEach(bindRow);

    addButton.addEventListener("click", () => {
        const index = tableBody.querySelectorAll("tr.item-row").length;
        const html = template.innerHTML.replaceAll("__INDEX__", index.toString());
        tableBody.insertAdjacentHTML("beforeend", html);
        const newRow = tableBody.lastElementChild;
        bindRow(newRow);
        reindex();
        applySmallBusinessMode();
        recalculateTotals();
    });

    if (smallBusinessCheckbox) {
        smallBusinessCheckbox.addEventListener("change", () => {
            applySmallBusinessMode();
            recalculateTotals();
        });
    }

    reindex();
    applySmallBusinessMode();
    recalculateTotals();
})();
