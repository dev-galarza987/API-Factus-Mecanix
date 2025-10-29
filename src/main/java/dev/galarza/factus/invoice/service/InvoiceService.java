package dev.galarza.factus.invoice.service;

import dev.galarza.factus.client.entity.Client;
import dev.galarza.factus.client.repository.ClientRepository;
import dev.galarza.factus.detail.entity.Detail;
import dev.galarza.factus.detail.mapper.DetailMapper;
import dev.galarza.factus.exception.BadRequestException;
import dev.galarza.factus.exception.ResourceNotFoundException;
import dev.galarza.factus.invoice.dto.InvoiceRequestDTO;
import dev.galarza.factus.invoice.dto.InvoiceResponseDTO;
import dev.galarza.factus.invoice.entity.Invoice;
import dev.galarza.factus.invoice.entity.InvoiceStatus;
import dev.galarza.factus.invoice.mapper.InvoiceMapper;
import dev.galarza.factus.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final InvoiceMapper invoiceMapper;
    private final DetailMapper detailMapper;

    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO requestDTO) {
        // Validar que el cliente existe
        Client client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", requestDTO.getClientId()));

        // Generar número de factura
        String numeroFactura = generateInvoiceNumber(requestDTO.getSerie());

        // Crear la factura
        Invoice invoice = Invoice.builder()
                .numeroFactura(numeroFactura)
                .serie(requestDTO.getSerie())
                .fechaEmision(requestDTO.getFechaEmision())
                .client(client)
                .tipoComprobante(requestDTO.getTipoComprobante())
                .observaciones(requestDTO.getObservaciones())
                .estado(InvoiceStatus.BORRADOR)
                .build();

        // Agregar los detalles
        requestDTO.getDetalles().forEach(detailDTO -> {
            Detail detail = detailMapper.toEntity(detailDTO);
            if (detail.getDescuento() == null) {
                detail.setDescuento(java.math.BigDecimal.ZERO);
            }
            invoice.addDetalle(detail);
        });

        // Calcular totales
        invoice.calcularTotales();

        // Guardar
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toResponseDTO(savedInvoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", id));
        return invoiceMapper.toResponseDTO(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoiceByNumber(String numeroFactura) {
        Invoice invoice = invoiceRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "número", numeroFactura));
        return invoiceMapper.toResponseDTO(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(invoiceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getInvoicesByClient(Long clientId) {
        return invoiceRepository.findByClientId(clientId).stream()
                .map(invoiceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByEstado(status).stream()
                .map(invoiceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByDateRange(startDate, endDate).stream()
                .map(invoiceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceResponseDTO updateInvoiceStatus(Long id, InvoiceStatus newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", id));

        // Validar transiciones de estado
        validateStatusTransition(invoice.getEstado(), newStatus);

        invoice.setEstado(newStatus);
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toResponseDTO(updatedInvoice);
    }

    @Transactional
    public InvoiceResponseDTO emitInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", id));

        if (invoice.getEstado() != InvoiceStatus.BORRADOR) {
            throw new BadRequestException("Solo se pueden emitir facturas en estado BORRADOR");
        }

        invoice.setEstado(InvoiceStatus.EMITIDA);
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toResponseDTO(updatedInvoice);
    }

    @Transactional
    public InvoiceResponseDTO cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", id));

        if (invoice.getEstado() == InvoiceStatus.ANULADA) {
            throw new BadRequestException("La factura ya está anulada");
        }

        if (invoice.getEstado() == InvoiceStatus.PAGADA) {
            throw new BadRequestException("No se puede anular una factura pagada");
        }

        invoice.setEstado(InvoiceStatus.ANULADA);
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toResponseDTO(updatedInvoice);
    }

    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", id));

        if (invoice.getEstado() != InvoiceStatus.BORRADOR) {
            throw new BadRequestException("Solo se pueden eliminar facturas en estado BORRADOR");
        }

        invoiceRepository.delete(invoice);
    }

    // Métodos privados auxiliares

    private String generateInvoiceNumber(String serie) {
        Integer lastNumber = invoiceRepository.findLastNumberBySerie(serie);
        int nextNumber = (lastNumber != null ? lastNumber : 0) + 1;
        return String.format("%s-%08d", serie, nextNumber);
    }

    private void validateStatusTransition(InvoiceStatus currentStatus, InvoiceStatus newStatus) {
        // Reglas de transición de estados
        if (currentStatus == InvoiceStatus.ANULADA) {
            throw new BadRequestException("No se puede cambiar el estado de una factura anulada");
        }

        if (currentStatus == InvoiceStatus.PAGADA && newStatus != InvoiceStatus.ANULADA) {
            throw new BadRequestException("Una factura pagada solo puede ser anulada");
        }

        if (currentStatus == InvoiceStatus.BORRADOR &&
            newStatus != InvoiceStatus.EMITIDA &&
            newStatus != InvoiceStatus.ANULADA) {
            throw new BadRequestException("Un borrador solo puede pasar a EMITIDA o ANULADA");
        }
    }
}

