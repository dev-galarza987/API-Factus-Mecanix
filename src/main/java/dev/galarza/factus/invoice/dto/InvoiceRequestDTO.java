package dev.galarza.factus.invoice.dto;

import dev.galarza.factus.detail.dto.DetailRequestDTO;
import dev.galarza.factus.invoice.entity.TipoComprobante;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequestDTO {

    @NotBlank(message = "La serie es obligatoria")
    @Size(max = 20)
    private String serie;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    @NotNull(message = "El cliente es obligatorio")
    private Long clientId;

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private TipoComprobante tipoComprobante;

    @NotEmpty(message = "Debe incluir al menos un detalle")
    private List<DetailRequestDTO> detalles;

    @Size(max = 500)
    private String observaciones;
}

