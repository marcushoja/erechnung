package de.hojam2.erechnung.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class InvoiceLineItem {

    @NotNull(message = "Anzahl ist erforderlich.")
    @DecimalMin(value = "0.01", message = "Anzahl muss größer als 0 sein.")
    @Digits(integer = 10, fraction = 2, message = "Anzahl darf maximal 2 Nachkommastellen haben.")
    private BigDecimal quantity;

    @NotBlank(message = "Beschreibung ist erforderlich.")
    private String description;

    @NotNull(message = "Einzelpreis ist erforderlich.")
    @DecimalMin(value = "0.00", message = "Einzelpreis darf nicht negativ sein.")
    @Digits(integer = 12, fraction = 2, message = "Einzelpreis darf maximal 2 Nachkommastellen haben.")
    private BigDecimal unitPriceNet;

    @NotNull(message = "Steuersatz ist erforderlich.")
    private VatRate vatRate;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPriceNet() {
        return unitPriceNet;
    }

    public void setUnitPriceNet(BigDecimal unitPriceNet) {
        this.unitPriceNet = unitPriceNet;
    }

    public VatRate getVatRate() {
        return vatRate;
    }

    public void setVatRate(VatRate vatRate) {
        this.vatRate = vatRate;
    }
}
