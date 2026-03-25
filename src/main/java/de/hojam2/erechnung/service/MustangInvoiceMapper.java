package de.hojam2.erechnung.service;

import de.hojam2.erechnung.model.InvoiceFormData;
import de.hojam2.erechnung.model.InvoiceLineItem;
import java.time.ZoneId;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.mustangproject.BankDetails;
import org.mustangproject.Invoice;
import org.mustangproject.Item;
import org.mustangproject.Product;
import org.mustangproject.TradeParty;
import org.springframework.stereotype.Component;

@Component
public class MustangInvoiceMapper {

    public Invoice toMustangInvoice(InvoiceFormData formData) {
        TradeParty sender = createSender(formData);
        TradeParty recipient = createRecipient(formData);

        Invoice invoice = new Invoice()
            .setSender(sender)
            .setRecipient(recipient)
            .setNumber(formData.getMeta().getInvoiceNumber())
            .setIssueDate(toDate(formData.getMeta().getInvoiceDate()))
            .setDeliveryDate(toDate(formData.getMeta().getServiceDate()))
            .setDueDate(toDate(formData.getMeta().getDueDate()))
            .setCurrency("EUR")
            .setOwnOrganisationName(formData.getSeller().getName())
            .setOwnTaxID(formData.getSeller().getTaxNumber())
            .setPaymentTermDescription("Zahlbar innerhalb von " + formData.getMeta().getPaymentTargetDays() + " Tagen");

        if (StringUtils.isNotBlank(formData.getBuyer().getBuyerReference())) {
            invoice.setReferenceNumber(formData.getBuyer().getBuyerReference());
        }
        if (StringUtils.isNotBlank(formData.getSeller().getVatId())) {
            invoice.setOwnVATID(formData.getSeller().getVatId());
        }

        int lineNo = 1;
        for (InvoiceLineItem lineItem : formData.getItems()) {
            Product product = new Product()
                .setName(lineItem.getDescription())
                .setDescription(lineItem.getDescription())
                .setUnit("C62")
                .setVATPercent(lineItem.getVatRate().percentage());

            Item item = new Item()
                .setId(String.valueOf(lineNo++))
                .setProduct(product)
                .setQuantity(lineItem.getQuantity())
                .setPrice(lineItem.getUnitPriceNet())
                .setBasisQuantity(java.math.BigDecimal.ONE);
            invoice.addItem(item);
        }
        return invoice;
    }

    private TradeParty createSender(InvoiceFormData formData) {
        TradeParty sender = new TradeParty(formData.getSeller().getName(), formData.getSeller().getStreet(), formData.getSeller().getZip(), formData.getSeller().getCity(), "DE")
            .setTaxID(formData.getSeller().getTaxNumber());

        if (StringUtils.isNotBlank(formData.getSeller().getVatId())) {
            sender.setVATID(formData.getSeller().getVatId());
        }
        BankDetails bankDetails = new BankDetails().setIBAN(formData.getSeller().getIban());
        if (StringUtils.isNotBlank(formData.getSeller().getBic())) {
            bankDetails.setBIC(formData.getSeller().getBic());
        }
        sender.addBankDetails(bankDetails);
        return sender;
    }

    private TradeParty createRecipient(InvoiceFormData formData) {
        TradeParty recipient = new TradeParty(formData.getBuyer().getName(), formData.getBuyer().getStreet(), formData.getBuyer().getZip(), formData.getBuyer().getCity(), "DE");
        if (StringUtils.isNotBlank(formData.getBuyer().getVatId())) {
            recipient.setVATID(formData.getBuyer().getVatId());
        }
        return recipient;
    }

    private Date toDate(java.time.LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
