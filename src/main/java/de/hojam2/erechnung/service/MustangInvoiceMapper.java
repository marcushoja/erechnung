package de.hojam2.erechnung.service;

import de.hojam2.erechnung.model.InvoiceFormData;
import de.hojam2.erechnung.model.InvoiceLineItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.mustangproject.BankDetails;
import org.mustangproject.Contact;
import org.mustangproject.Invoice;
import org.mustangproject.Item;
import org.mustangproject.Product;
import org.mustangproject.TradeParty;
import org.springframework.stereotype.Component;

@Component
public class MustangInvoiceMapper {

    public static final String SUBJECT_PREFIX = "SUBJECT::";
    public static final String PAYMENT_HINT_PREFIX = "PAYMENT_HINT::";

    public Invoice toMustangInvoice(InvoiceFormData formData) {
        TradeParty sender = createSender(formData);
        TradeParty recipient = createRecipient(formData);

        Invoice invoice = new Invoice()
            .setSender(sender)
            .setRecipient(recipient)
            .setDocumentName(formData.getMeta().getSubject())
            .setNumber(formData.getMeta().getInvoiceNumber())
            .setIssueDate(toDate(formData.getMeta().getInvoiceDate()))
            .setDeliveryDate(toDate(resolveDeliveryDate(formData)))
            .setDueDate(toDate(formData.getMeta().getDueDate()))
            .setCurrency("EUR")
            .setOwnOrganisationName(formData.getSeller().getName())
            .setOwnTaxID(formData.getSeller().getTaxNumber())
            .setPaymentTermDescription(resolvePaymentTermDescription(formData));

        if (formData.getMeta().getServicePeriodStart() != null && formData.getMeta().getServicePeriodEnd() != null) {
            invoice.setDetailedDeliveryPeriod(
                toDate(formData.getMeta().getServicePeriodStart()),
                toDate(formData.getMeta().getServicePeriodEnd())
            );
        }

        if (StringUtils.isNotBlank(formData.getBuyer().getBuyerReference())) {
            invoice.setReferenceNumber(formData.getBuyer().getBuyerReference());
        }
        if (StringUtils.isNotBlank(formData.getSeller().getVatId())) {
            invoice.setOwnVATID(formData.getSeller().getVatId());
        }

        if (formData.isSmallBusinessRegulation()) {
            invoice.addTaxNote("Gemäß § 19 UStG wird aufgrund der Kleinunternehmerregelung keine Umsatzsteuer erhoben");
        }
        if (StringUtils.isNotBlank(formData.getMeta().getSubject())) {
            invoice.addNote(SUBJECT_PREFIX + formData.getMeta().getSubject());
        }
        if (StringUtils.isNotBlank(formData.getPaymentInstructionText())) {
            invoice.addNote(PAYMENT_HINT_PREFIX + formData.getPaymentInstructionText());
        }

        int lineNo = 1;
        for (InvoiceLineItem lineItem : formData.getItems()) {
            BigDecimal vatPercent = formData.isSmallBusinessRegulation()
                ? BigDecimal.ZERO
                : lineItem.getVatRate().percentage();
            Product product = new Product()
                .setName(lineItem.getDescription())
                .setDescription(lineItem.getDescription())
                .setUnit("C62")
                .setVATPercent(vatPercent);

            Item item = new Item()
                .setId(String.valueOf(lineNo++))
                .setProduct(product)
                .setQuantity(scaleToTwo(lineItem.getQuantity()))
                .setPrice(scaleToTwo(lineItem.getUnitPriceNet()))
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
        if (StringUtils.isNotBlank(formData.getSeller().getEmail())) {
            sender.setEmail(formData.getSeller().getEmail());
        }
        if (StringUtils.isNotBlank(formData.getSeller().getPhone()) || StringUtils.isNotBlank(formData.getSeller().getEmail())) {
            sender.setContact(new Contact()
                .setPhone(formData.getSeller().getPhone())
                .setEMail(formData.getSeller().getEmail()));
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

    private java.time.LocalDate resolveDeliveryDate(InvoiceFormData formData) {
        if (formData.getMeta().getServiceDate() != null) {
            return formData.getMeta().getServiceDate();
        }
        return formData.getMeta().getServicePeriodStart();
    }

    private String resolvePaymentTermDescription(InvoiceFormData formData) {
        return "Zahlbar innerhalb von " + formData.getMeta().getPaymentTargetDays() + " Tagen";
    }

    private BigDecimal scaleToTwo(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
