package de.hojam2.erechnung.model;

import java.math.BigDecimal;

public record TaxSummary(BigDecimal taxRate, BigDecimal netAmount, BigDecimal taxAmount) {
}
