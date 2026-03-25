package de.hojam2.erechnung.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class InvoiceFormData {

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
}
