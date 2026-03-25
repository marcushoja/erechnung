package de.hojam2.erechnung.controller;

import de.hojam2.erechnung.model.ExportFormat;
import de.hojam2.erechnung.model.InvoiceFormData;
import de.hojam2.erechnung.model.InvoiceLineItem;
import de.hojam2.erechnung.model.InvoiceTotals;
import de.hojam2.erechnung.model.VatRate;
import de.hojam2.erechnung.service.InvoiceCalculationService;
import de.hojam2.erechnung.service.InvoiceExportService;
import de.hojam2.erechnung.service.InvoiceImportService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class InvoiceController {

    private final InvoiceCalculationService calculationService;
    private final InvoiceExportService exportService;
    private final InvoiceImportService importService;

    public InvoiceController(InvoiceCalculationService calculationService,
                             InvoiceExportService exportService,
                             InvoiceImportService importService) {
        this.calculationService = calculationService;
        this.exportService = exportService;
        this.importService = importService;
    }

    @GetMapping("/")
    public String index(Model model) {
        InvoiceFormData formData = defaultFormData();
        model.addAttribute("invoiceFormData", formData);
        model.addAttribute("vatRates", VatRate.values());
        model.addAttribute("formats", ExportFormat.values());
        model.addAttribute("totals", calculationService.calculate(formData));
        return "index";
    }

    @PostMapping("/import")
    public String importInvoice(@RequestParam("file") MultipartFile file, Model model) {
        InvoiceFormData formData;
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Bitte eine Datei auswählen.");
            }
            formData = importService.importFromFile(file.getBytes());
            model.addAttribute("successMessage", "Datei erfolgreich importiert.");
        } catch (Exception ex) {
            formData = defaultFormData();
            model.addAttribute("errorMessage", "Datei konnte nicht geparst werden / Keine gültigen Rechnungsdaten gefunden.");
        }

        model.addAttribute("invoiceFormData", formData);
        model.addAttribute("vatRates", VatRate.values());
        model.addAttribute("formats", ExportFormat.values());
        model.addAttribute("totals", calculationService.calculate(formData));
        return "index";
    }

    @PostMapping("/export")
    public Object exportInvoice(@Valid @ModelAttribute("invoiceFormData") InvoiceFormData formData,
                                BindingResult bindingResult,
                                Model model) throws IOException {
        if (formData.getItems() == null || formData.getItems().isEmpty()) {
            bindingResult.reject("items.empty", "Mindestens eine Rechnungsposition ist erforderlich.");
        }

        for (int i = 0; i < formData.getItems().size(); i++) {
            InvoiceLineItem item = formData.getItems().get(i);
            if (item.getQuantity() != null && item.getUnitPriceNet() != null
                && item.getQuantity().multiply(item.getUnitPriceNet()).compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("items[" + i + "].unitPriceNet", "line.zero", "Positionswert muss größer als 0 sein.");
            }
        }

        InvoiceTotals totals = calculationService.calculate(formData);
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Bitte Pflichtfelder prüfen. Export wurde blockiert.");
            model.addAttribute("vatRates", VatRate.values());
            model.addAttribute("formats", ExportFormat.values());
            model.addAttribute("totals", totals);
            return "index";
        }

        String customerName = sanitizeFilenamePart(formData.getBuyer().getName());
        String invoiceNumber = sanitizeFilenamePart(formData.getMeta().getInvoiceNumber());

        byte[] content;
        String extension;
        MediaType mediaType;

        if (formData.getExportFormat() == ExportFormat.XRECHNUNG_XML) {
            content = exportService.exportXRechnungXml(formData);
            extension = "xml";
            mediaType = MediaType.APPLICATION_XML;
        } else {
            content = exportService.exportZugferdPdf(formData, totals);
            extension = "pdf";
            mediaType = MediaType.APPLICATION_PDF;
        }

        String fileName = "Rechnung_" + invoiceNumber + "_" + customerName + "." + extension;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(fileName, StandardCharsets.UTF_8)
            .build());

        return ResponseEntity.ok()
            .headers(headers)
            .body(content);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    private InvoiceFormData defaultFormData() {
        InvoiceFormData formData = new InvoiceFormData();
        formData.getMeta().setInvoiceDate(LocalDate.now());
        formData.getMeta().setServiceDate(LocalDate.now());
        formData.getMeta().setPaymentTargetDays(14);

        InvoiceLineItem firstItem = new InvoiceLineItem();
        firstItem.setQuantity(BigDecimal.ONE);
        firstItem.setUnitPriceNet(new BigDecimal("0.00"));
        firstItem.setVatRate(VatRate.VAT_19);
        firstItem.setDescription("");
        formData.getItems().add(firstItem);
        return formData;
    }

    private String sanitizeFilenamePart(String raw) {
        if (StringUtils.isBlank(raw)) {
            return "Unbekannt";
        }
        return raw.replaceAll("[^a-zA-Z0-9ÄÖÜäöüß_-]", "_");
    }
}
