package de.hojam2.erechnung.model;

import java.math.BigDecimal;

public enum VatRate {
    VAT_19(new BigDecimal("19"), "19%"),
    VAT_7(new BigDecimal("7"), "7%"),
    VAT_0(BigDecimal.ZERO, "0%");

    private final BigDecimal percentage;
    private final String label;

    VatRate(BigDecimal percentage, String label) {
        this.percentage = percentage;
        this.label = label;
    }

    public BigDecimal percentage() {
        return percentage;
    }

    public String label() {
        return label;
    }

    public static VatRate fromPercentage(BigDecimal percent) {
        if (percent == null) {
            return VAT_19;
        }
        for (VatRate rate : values()) {
            if (rate.percentage.compareTo(percent.stripTrailingZeros()) == 0
                || rate.percentage.compareTo(percent) == 0) {
                return rate;
            }
        }
        return VAT_19;
    }
}
