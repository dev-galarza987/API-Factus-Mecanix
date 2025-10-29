package dev.galarza.factus.invoice.dto;

import dev.galarza.factus.client.dto.ClientResponseDTO;
import dev.galarza.factus.detail.dto.DetailResponseDTO;
import dev.galarza.factus.invoice.entity.InvoiceStatus;
import dev.galarza.factus.invoice.entity.TipoComprobante;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDTO {

    private Long id;
    private String numeroFactura;
    private String serie;
    private LocalDate fechaEmision;
    private ClientResponseDTO client;
    private List<DetailResponseDTO> detalles;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal it;
    private BigDecimal total;
    private InvoiceStatus estado;
    private TipoComprobante tipoComprobante;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

