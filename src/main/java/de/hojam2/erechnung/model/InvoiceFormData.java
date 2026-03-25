package de.hojam2.erechnung.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class InvoiceFormData {

    public static final String DEFAULT_PAYMENT_INSTRUCTION = "Bitte überweisen Sie den Rechnungsbetrag innerhalb der nächsten 14 Tage auf das unten genannte Konto.";

    @Valid
    @NotNull
    private SellerData seller = new SellerData();

    @Valid
    @NotNull
    private BuyerData buyer = new BuyerData();

    @Valid
    @NotNull
    private InvoiceMetaData meta = new InvoiceMetaData();

    @Valid
    @NotEmpty(message = "Mindestens eine Rechnungsposition ist erforderlich.")
    private List<InvoiceLineItem> items = new ArrayList<>();

    @NotNull(message = "Exportformat ist erforderlich.")
    private ExportFormat exportFormat = ExportFormat.ZUGFERD_PDF;

    private boolean smallBusinessRegulation;

    private String paymentInstructionText = DEFAULT_PAYMENT_INSTRUCTION;

    public SellerData getSeller() {
        return seller;
    }

    public void setSeller(SellerData seller) {
        this.seller = seller;
    }

    public BuyerData getBuyer() {
        return buyer;
    }

    public void setBuyer(BuyerData buyer) {
        this.buyer = buyer;
    }

    public InvoiceMetaData getMeta() {
        return meta;
    }

    public void setMeta(InvoiceMetaData meta) {
        this.meta = meta;
    }

    public List<InvoiceLineItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceLineItem> items) {
        this.items = items;
    }

    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(ExportFormat exportFormat) {
        this.exportFormat = exportFormat;
    }

    public boolean isSmallBusinessRegulation() {
        return smallBusinessRegulation;
    }

    public void setSmallBusinessRegulation(boolean smallBusinessRegulation) {
        this.smallBusinessRegulation = smallBusinessRegulation;
    }

    public String getPaymentInstructionText() {
        return paymentInstructionText;
    }

    public void setPaymentInstructionText(String paymentInstructionText) {
        this.paymentInstructionText = paymentInstructionText;
    }
}
