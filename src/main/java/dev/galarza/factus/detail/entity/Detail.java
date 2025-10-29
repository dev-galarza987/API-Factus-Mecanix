package dev.galarza.factus.detail.entity;

import dev.galarza.factus.invoice.entity.Invoice;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Detail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @NotNull(message = "La factura es obligatoria")
    private Invoice invoice;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(nullable = false, length = 255)
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(length = 50)
    private String unidadMedida; // ej: UND, KG, LTS, etc.

    @Column(length = 50)
    private String codigoProducto;

    // Método para calcular subtotal
    @PrePersist
    @PreUpdate
    public void calcularSubtotal() {
        BigDecimal cantidadBD = new BigDecimal(cantidad);
        BigDecimal totalSinDescuento = precioUnitario.multiply(cantidadBD);
        this.subtotal = totalSinDescuento.subtract(descuento);
    }
}

