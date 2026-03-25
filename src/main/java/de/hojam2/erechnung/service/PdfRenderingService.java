package de.hojam2.erechnung.service;

import de.hojam2.erechnung.model.InvoiceFormData;
import de.hojam2.erechnung.model.InvoiceTotals;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
public class PdfRenderingService {

    private final TemplateEngine templateEngine;

    public PdfRenderingService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] renderInvoicePdf(InvoiceFormData formData, InvoiceTotals totals) {
        Context context = new Context();
        context.setVariable("invoice", formData);
        context.setVariable("totals", totals);
        context.setVariable("dueDate", formData.getMeta().getDueDate());
        context.setVariable("today", LocalDate.now());
        context.setVariable("paymentTermsText", "Zahlung per SEPA-Überweisung auf das Konto");

        String html = templateEngine.process("pdf/invoice", context);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(new String(html.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        renderer.layout();
        renderer.createPDF(outputStream);
        renderer.finishPDF();
        return outputStream.toByteArray();
    }
}
