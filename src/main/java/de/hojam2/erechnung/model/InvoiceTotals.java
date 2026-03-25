package de.hojam2.erechnung.model;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceTotals(BigDecimal netTotal, BigDecimal grossTotal, BigDecimal taxTotal, List<TaxSummary> taxSummaries) {
}
