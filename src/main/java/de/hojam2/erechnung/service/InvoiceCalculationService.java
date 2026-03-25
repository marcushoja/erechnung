package de.hojam2.erechnung.service;

import de.hojam2.erechnung.model.InvoiceFormData;
import de.hojam2.erechnung.model.InvoiceLineItem;
import de.hojam2.erechnung.model.InvoiceTotals;
import de.hojam2.erechnung.model.TaxSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class InvoiceCalculationService {

    private static final int MONEY_SCALE = 2;

    public InvoiceTotals calculate(InvoiceFormData formData) {
        BigDecimal netTotal = BigDecimal.ZERO;
        Map<BigDecimal, BigDecimal> netPerRate = new LinkedHashMap<>();

        for (InvoiceLineItem item : formData.getItems()) {
            if (item.getQuantity() == null || item.getUnitPriceNet() == null || item.getVatRate() == null) {
                continue;
            }
            BigDecimal lineNet = item.getQuantity().multiply(item.getUnitPriceNet());
            BigDecimal effectiveRate = formData.isSmallBusinessRegulation() ? BigDecimal.ZERO : item.getVatRate().percentage();
            netTotal = netTotal.add(lineNet);
            netPerRate.merge(effectiveRate, lineNet, BigDecimal::add);
        }

        netTotal = normalize(netTotal);
        List<TaxSummary> taxSummaries = new ArrayList<>();
        BigDecimal taxTotal = BigDecimal.ZERO;
        for (Map.Entry<BigDecimal, BigDecimal> entry : netPerRate.entrySet()) {
            BigDecimal rate = entry.getKey();
            BigDecimal groupedNet = normalize(entry.getValue());
            BigDecimal groupedTax = normalize(groupedNet.multiply(rate).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            taxSummaries.add(new TaxSummary(rate, groupedNet, groupedTax));
            taxTotal = taxTotal.add(groupedTax);
        }
        taxSummaries.sort(Comparator.comparing(TaxSummary::taxRate).reversed());
        taxTotal = normalize(taxTotal);
        BigDecimal grossTotal = normalize(netTotal.add(taxTotal));

        return new InvoiceTotals(netTotal, grossTotal, taxTotal, taxSummaries);
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
