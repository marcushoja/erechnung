package de.hojam2.erechnung.service;

import de.hojam2.erechnung.model.InvoiceFormData;
import de.hojam2.erechnung.model.InvoiceTotals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.mustangproject.EStandard;
import org.mustangproject.Invoice;
import org.mustangproject.ZUGFeRD.Profile;
import org.mustangproject.ZUGFeRD.Profiles;
import org.mustangproject.ZUGFeRD.ZUGFeRD2PullProvider;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;
import org.springframework.stereotype.Service;

@Service
public class InvoiceExportService {

    private final MustangInvoiceMapper mustangInvoiceMapper;
    private final PdfRenderingService pdfRenderingService;

    public InvoiceExportService(MustangInvoiceMapper mustangInvoiceMapper, PdfRenderingService pdfRenderingService) {
        this.mustangInvoiceMapper = mustangInvoiceMapper;
        this.pdfRenderingService = pdfRenderingService;
    }

    public byte[] exportXRechnungXml(InvoiceFormData formData) throws IOException {
        Invoice invoice = mustangInvoiceMapper.toMustangInvoice(formData);
        ZUGFeRD2PullProvider xmlProvider = new ZUGFeRD2PullProvider();

        Profile profile = Profiles.getByName(EStandard.facturx, "EN16931", 2);
        if (profile == null) {
            profile = Profiles.getByName("EN16931", 2);
        }
        if (profile == null) {
            profile = Profiles.getByName("EN16931");
        }
        if (profile != null) {
            xmlProvider.setProfile(profile);
        }

        xmlProvider.generateXML(invoice);
        return xmlProvider.getXML();
    }

    public byte[] exportZugferdPdf(InvoiceFormData formData, InvoiceTotals totals) throws IOException {
        Invoice invoice = mustangInvoiceMapper.toMustangInvoice(formData);
        byte[] basePdf = pdfRenderingService.renderInvoicePdf(formData, totals);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3();
        try {
            Profile profile = Profiles.getByName(EStandard.facturx, "EN16931", 2);
            if (profile == null) {
                profile = Profiles.getByName("EN16931", 2);
            }
            exporter
                .load(basePdf)
                .setProfile(profile)
                .setTransaction(invoice)
                .export(outputStream);
        } finally {
            exporter.close();
        }

        return outputStream.toByteArray();
    }
}
