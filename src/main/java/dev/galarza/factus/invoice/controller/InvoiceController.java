package dev.galarza.factus.invoice.controller;

import dev.galarza.factus.invoice.dto.InvoiceRequestDTO;
import dev.galarza.factus.invoice.dto.InvoiceResponseDTO;
import dev.galarza.factus.invoice.entity.InvoiceStatus;
import dev.galarza.factus.invoice.service.InvoiceService;
import dev.galarza.factus.invoice.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Facturas", description = "API de gestión de facturas")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfService pdfService;

    @PostMapping
    @Operation(summary = "Crear nueva factura")
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO requestDTO) {
        InvoiceResponseDTO response = invoiceService.createInvoice(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener factura por ID")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        InvoiceResponseDTO response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{numeroFactura}")
    @Operation(summary = "Obtener factura por número")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceByNumber(@PathVariable String numeroFactura) {
        InvoiceResponseDTO response = invoiceService.getInvoiceByNumber(numeroFactura);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas las facturas")
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        List<InvoiceResponseDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Listar facturas por cliente")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByClient(@PathVariable Long clientId) {
        List<InvoiceResponseDTO> invoices = invoiceService.getInvoicesByClient(clientId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar facturas por estado")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByStatus(@PathVariable InvoiceStatus status) {
        List<InvoiceResponseDTO> invoices = invoiceService.getInvoicesByStatus(status);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Listar facturas por rango de fechas")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<InvoiceResponseDTO> invoices = invoiceService.getInvoicesByDateRange(startDate, endDate);
        return ResponseEntity.ok(invoices);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de factura")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam InvoiceStatus status) {
        InvoiceResponseDTO response = invoiceService.updateInvoiceStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/emit")
    @Operation(summary = "Emitir factura")
    public ResponseEntity<InvoiceResponseDTO> emitInvoice(@PathVariable Long id) {
        InvoiceResponseDTO response = invoiceService.emitInvoice(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Anular factura")
    public ResponseEntity<InvoiceResponseDTO> cancelInvoice(@PathVariable Long id) {
        InvoiceResponseDTO response = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar factura (solo borradores)")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Descargar factura en PDF")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        byte[] pdfBytes = pdfService.generateInvoicePdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "factura-" + id + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
