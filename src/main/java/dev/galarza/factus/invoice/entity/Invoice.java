package dev.galarza.factus.invoice.entity;

import dev.galarza.factus.client.entity.Client;
import dev.galarza.factus.detail.entity.Detail;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número de factura es obligatorio")
    @Column(name = "numero_factura", nullable = false, unique = true, length = 50)
    private String numeroFactura;

    @NotBlank(message = "La serie es obligatoria")
    @Column(nullable = false, length = 20)
    private String serie;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    private Client client;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Detail> detalles = new ArrayList<>();

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @NotNull
    @Column(name = "iva", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal iva = BigDecimal.ZERO; // 13%

    @NotNull
    @Column(name = "it", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal it = BigDecimal.ZERO; // 3%

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus estado = InvoiceStatus.BORRADOR;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 30)
    @Builder.Default
    private TipoComprobante tipoComprobante = TipoComprobante.FACTURA;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Métodos de ayuda para la relación bidireccional
    public void addDetalle(Detail detail) {
        detalles.add(detail);
        detail.setInvoice(this);
    }

    public void removeDetalle(Detail detail) {
        detalles.remove(detail);
        detail.setInvoice(null);
    }

    // Método para calcular totales
    public void calcularTotales() {
        this.subtotal = detalles.stream()
            .map(Detail::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // IVA 13%
        this.iva = subtotal.multiply(new BigDecimal("0.13"));

        // IT 3%
        this.it = subtotal.multiply(new BigDecimal("0.03"));

        // Total
        this.total = subtotal.add(iva).add(it);
    }
}

