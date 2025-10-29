package dev.galarza.factus.invoice.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import dev.galarza.factus.detail.entity.Detail;
import dev.galarza.factus.exception.ResourceNotFoundException;
import dev.galarza.factus.invoice.entity.Invoice;
import dev.galarza.factus.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final InvoiceRepository invoiceRepository;
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "BO"));
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", invoiceId));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título
            Paragraph title = new Paragraph("FACTURA ELECTRÓNICA")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Información de la empresa
            document.add(new Paragraph("GALARZA TECHCORP")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Sistema de Facturación Electrónica")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // Información de la factura
            Table infoTable = new Table(2);
            infoTable.setWidth(UnitValue.createPercentValue(100));

            infoTable.addCell(createCell("Número de Factura:", true));
            infoTable.addCell(createCell(invoice.getNumeroFactura(), false));

            infoTable.addCell(createCell("Serie:", true));
            infoTable.addCell(createCell(invoice.getSerie(), false));

            infoTable.addCell(createCell("Tipo:", true));
            infoTable.addCell(createCell(invoice.getTipoComprobante().toString(), false));

            infoTable.addCell(createCell("Fecha de Emisión:", true));
            infoTable.addCell(createCell(invoice.getFechaEmision().format(dateFormatter), false));

            infoTable.addCell(createCell("Estado:", true));
            infoTable.addCell(createCell(invoice.getEstado().toString(), false));

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Información del cliente
            document.add(new Paragraph("DATOS DEL CLIENTE")
                    .setFontSize(12)
                    .setBold()
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            Table clientTable = new Table(2);
            clientTable.setWidth(UnitValue.createPercentValue(100));

            clientTable.addCell(createCell("Cliente:", true));
            clientTable.addCell(createCell(invoice.getClient().getNombre() + " " +
                    invoice.getClient().getApellido(), false));

            clientTable.addCell(createCell("NIT:", true));
            clientTable.addCell(createCell(invoice.getClient().getNit().toString(), false));

            if (invoice.getClient().getEmail() != null) {
                clientTable.addCell(createCell("Email:", true));
                clientTable.addCell(createCell(invoice.getClient().getEmail(), false));
            }

            if (invoice.getClient().getTelefono() != null) {
                clientTable.addCell(createCell("Teléfono:", true));
                clientTable.addCell(createCell(invoice.getClient().getTelefono(), false));
            }

            if (invoice.getClient().getDireccion() != null) {
                clientTable.addCell(createCell("Dirección:", true));
                clientTable.addCell(createCell(invoice.getClient().getDireccion(), false));
            }

            document.add(clientTable);
            document.add(new Paragraph("\n"));

            // Detalles de la factura
            document.add(new Paragraph("DETALLE DE LA FACTURA")
                    .setFontSize(12)
                    .setBold()
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));

            Table detailsTable = new Table(new float[]{1, 4, 2, 2, 2, 2});
            detailsTable.setWidth(UnitValue.createPercentValue(100));

            // Encabezados
            detailsTable.addHeaderCell(createHeaderCell("#"));
            detailsTable.addHeaderCell(createHeaderCell("Descripción"));
            detailsTable.addHeaderCell(createHeaderCell("Cant."));
            detailsTable.addHeaderCell(createHeaderCell("P. Unit."));
            detailsTable.addHeaderCell(createHeaderCell("Desc."));
            detailsTable.addHeaderCell(createHeaderCell("Subtotal"));

            // Filas de detalles
            int index = 1;
            for (Detail detail : invoice.getDetalles()) {
                detailsTable.addCell(createCell(String.valueOf(index++), false));
                detailsTable.addCell(createCell(detail.getDescripcion(), false));
                detailsTable.addCell(createCell(String.valueOf(detail.getCantidad()), false));
                detailsTable.addCell(createCell(formatCurrency(detail.getPrecioUnitario()), false));
                detailsTable.addCell(createCell(formatCurrency(detail.getDescuento()), false));
                detailsTable.addCell(createCell(formatCurrency(detail.getSubtotal()), false));
            }

            document.add(detailsTable);
            document.add(new Paragraph("\n"));

            // Totales
            Table totalsTable = new Table(2);
            totalsTable.setWidth(UnitValue.createPercentValue(50));
            totalsTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

            totalsTable.addCell(createCell("Subtotal:", true));
            totalsTable.addCell(createCell(formatCurrency(invoice.getSubtotal()), false)
                    .setTextAlignment(TextAlignment.RIGHT));

            totalsTable.addCell(createCell("IVA (13%):", true));
            totalsTable.addCell(createCell(formatCurrency(invoice.getIva()), false)
                    .setTextAlignment(TextAlignment.RIGHT));

            totalsTable.addCell(createCell("IT (3%):", true));
            totalsTable.addCell(createCell(formatCurrency(invoice.getIt()), false)
                    .setTextAlignment(TextAlignment.RIGHT));

            totalsTable.addCell(createCell("TOTAL:", true)
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            totalsTable.addCell(createCell(formatCurrency(invoice.getTotal()), false)
                    .setFontSize(14)
                    .setBold()
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(totalsTable);

            // Observaciones
            if (invoice.getObservaciones() != null && !invoice.getObservaciones().isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("OBSERVACIONES:")
                        .setFontSize(10)
                        .setBold());
                document.add(new Paragraph(invoice.getObservaciones())
                        .setFontSize(9));
            }

            // Pie de página
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Gracias por su preferencia")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic());

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }

    private Cell createCell(String text, boolean bold) {
        Cell cell = new Cell().add(new Paragraph(text));
        if (bold) {
            cell.setBold();
        }
        return cell;
    }

    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setBold()
                .setBackgroundColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private String formatCurrency(BigDecimal amount) {
        return currencyFormat.format(amount);
    }
}

