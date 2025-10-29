package dev.galarza.factus.detail.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailResponseDTO {

    private Long id;
    private String descripcion;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuento;
    private BigDecimal subtotal;
    private String unidadMedida;
    private String codigoProducto;
}

