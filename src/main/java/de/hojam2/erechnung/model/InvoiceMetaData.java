package de.hojam2.erechnung.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class InvoiceMetaData {

    @NotBlank(message = "Rechnungsnummer ist erforderlich.")
    private String invoiceNumber;

    @NotNull(message = "Rechnungsdatum ist erforderlich.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate invoiceDate;

    @NotNull(message = "Liefer-/Leistungsdatum ist erforderlich.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate serviceDate;

    @NotNull(message = "Zahlungsziel in Tagen ist erforderlich.")
    @Min(value = 1, message = "Zahlungsziel muss mindestens 1 Tag sein.")
    private Integer paymentTargetDays;

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    public Integer getPaymentTargetDays() {
        return paymentTargetDays;
    }

    public void setPaymentTargetDays(Integer paymentTargetDays) {
        this.paymentTargetDays = paymentTargetDays;
    }

    @Nullable
    public LocalDate getDueDate() {
        if (invoiceDate == null || paymentTargetDays == null) {
            return null;
        }
        return invoiceDate.plusDays(paymentTargetDays);
    }
}
