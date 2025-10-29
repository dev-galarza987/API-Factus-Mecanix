package dev.galarza.factus.detail.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailRequestDTO {

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255)
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;

    private BigDecimal descuento;

    @Size(max = 50)
    private String unidadMedida;

    @Size(max = 50)
    private String codigoProducto;
}

