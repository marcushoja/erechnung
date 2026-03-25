package de.hojam2.erechnung.service;

import de.hojam2.erechnung.model.BuyerData;
import de.hojam2.erechnung.model.ExportFormat;
import de.hojam2.erechnung.model.InvoiceFormData;
import de.hojam2.erechnung.model.InvoiceLineItem;
import de.hojam2.erechnung.model.InvoiceMetaData;
import de.hojam2.erechnung.model.SellerData;
import de.hojam2.erechnung.model.VatRate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.mustangproject.BankDetails;
import org.mustangproject.Invoice;
import org.mustangproject.TradeParty;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;
import org.mustangproject.ZUGFeRD.ZUGFeRDInvoiceImporter;
import org.springframework.stereotype.Service;

@Service
public class InvoiceImportService {

    public InvoiceFormData importFromFile(byte[] rawBytes) throws Exception {
        Invoice parsed = parse(rawBytes);
        if (parsed == null) {
            throw new IllegalArgumentException("Keine gültigen Rechnungsdaten gefunden.");
        }

        InvoiceFormData formData = new InvoiceFormData();
        formData.setSeller(mapSeller(parsed));
        formData.setBuyer(mapBuyer(parsed));
        formData.setMeta(mapMeta(parsed));
        formData.setItems(mapItems(parsed));
        formData.setExportFormat(ExportFormat.ZUGFERD_PDF);
        if (formData.getItems().isEmpty()) {
            formData.getItems().add(defaultItem());
        }
        return formData;
    }

    private Invoice parse(byte[] bytes) throws Exception {
        try {
            ZUGFeRDInvoiceImporter importer = new ZUGFeRDInvoiceImporter(new ByteArrayInputStream(bytes));
            return importer.extractInvoice();
        } catch (Exception parseByStreamFailed) {
            ZUGFeRDInvoiceImporter fallbackImporter = new ZUGFeRDInvoiceImporter();
            fallbackImporter.setRawXML(bytes);
            return fallbackImporter.extractInvoice();
        }
    }

    private SellerData mapSeller(Invoice parsed) {
        SellerData seller = new SellerData();
        TradeParty sender = parsed.getSender();
        if (sender != null) {
            seller.setName(sender.getName());
            seller.setStreet(sender.getStreet());
            seller.setZip(sender.getZIP());
            seller.setCity(sender.getLocation());
            seller.setTaxNumber(StringUtils.defaultIfBlank(sender.getTaxID(), parsed.getOwnTaxID()));
            seller.setVatId(StringUtils.defaultIfBlank(sender.getVATID(), parsed.getOwnVATID()));
            List<BankDetails> bankDetails = sender.getBankDetails();
            if (bankDetails != null && !bankDetails.isEmpty()) {
                BankDetails first = bankDetails.get(0);
                seller.setIban(first.getIBAN());
                seller.setBic(first.getBIC());
            }
        }

        if (StringUtils.isBlank(seller.getName())) {
            seller.setName(parsed.getOwnOrganisationName());
        }
        if (StringUtils.isBlank(seller.getTaxNumber())) {
            seller.setTaxNumber(parsed.getOwnTaxID());
        }
        if (StringUtils.isBlank(seller.getVatId())) {
            seller.setVatId(parsed.getOwnVATID());
        }
        return seller;
    }

    private BuyerData mapBuyer(Invoice parsed) {
        BuyerData buyer = new BuyerData();
        TradeParty recipient = parsed.getRecipient();
        if (recipient != null) {
            buyer.setName(recipient.getName());
            buyer.setStreet(recipient.getStreet());
            buyer.setZip(recipient.getZIP());
            buyer.setCity(recipient.getLocation());
            buyer.setVatId(recipient.getVATID());
        }
        buyer.setBuyerReference(parsed.getReferenceNumber());
        return buyer;
    }

    private InvoiceMetaData mapMeta(Invoice parsed) {
        InvoiceMetaData metaData = new InvoiceMetaData();
        metaData.setInvoiceNumber(parsed.getNumber());

        LocalDate issueDate = toLocalDate(parsed.getIssueDate(), LocalDate.now());
        LocalDate serviceDate = toLocalDate(parsed.getDeliveryDate(), issueDate);
        metaData.setInvoiceDate(issueDate);
        metaData.setServiceDate(serviceDate);

        LocalDate dueDate = toLocalDate(parsed.getDueDate(), issueDate.plusDays(14));
        long days = ChronoUnit.DAYS.between(issueDate, dueDate);
        metaData.setPaymentTargetDays((int) Math.max(1, days));

        return metaData;
    }

    private List<InvoiceLineItem> mapItems(Invoice parsed) {
        List<InvoiceLineItem> items = new ArrayList<>();
        IZUGFeRDExportableItem[] importedItems = parsed.getZFItems();
        if (importedItems == null) {
            return items;
        }
        for (IZUGFeRDExportableItem importedItem : importedItems) {
            InvoiceLineItem item = new InvoiceLineItem();
            item.setQuantity(defaultBigDecimal(importedItem.getQuantity(), BigDecimal.ONE));
            item.setDescription(importedItem.getProduct() != null
                ? StringUtils.defaultIfBlank(importedItem.getProduct().getName(), importedItem.getProduct().getDescription())
                : "Position");
            item.setUnitPriceNet(defaultBigDecimal(importedItem.getPrice(), BigDecimal.ZERO));
            BigDecimal vat = importedItem.getProduct() != null ? importedItem.getProduct().getVATPercent() : new BigDecimal("19");
            item.setVatRate(VatRate.fromPercentage(vat));
            items.add(item);
        }
        return items;
    }

    private BigDecimal defaultBigDecimal(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private LocalDate toLocalDate(Date date, LocalDate fallback) {
        if (date == null) {
            return fallback;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private InvoiceLineItem defaultItem() {
        InvoiceLineItem item = new InvoiceLineItem();
        item.setQuantity(BigDecimal.ONE);
        item.setUnitPriceNet(BigDecimal.ZERO);
        item.setDescription("");
        item.setVatRate(VatRate.VAT_19);
        return item;
    }
}
