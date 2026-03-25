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

    @NotBlank(message = "Betreff ist erforderlich.")
    private String subject;

    @NotNull(message = "Rechnungsdatum ist erforderlich.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate invoiceDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate serviceDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate servicePeriodStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate servicePeriodEnd;

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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
    }

    public LocalDate getServicePeriodStart() {
        return servicePeriodStart;
    }

    public void setServicePeriodStart(LocalDate servicePeriodStart) {
        this.servicePeriodStart = servicePeriodStart;
    }

    public LocalDate getServicePeriodEnd() {
        return servicePeriodEnd;
    }

    public void setServicePeriodEnd(LocalDate servicePeriodEnd) {
        this.servicePeriodEnd = servicePeriodEnd;
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
