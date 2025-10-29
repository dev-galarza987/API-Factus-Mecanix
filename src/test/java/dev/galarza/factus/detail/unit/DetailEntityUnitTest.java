package dev.galarza.factus.detail.unit;

import dev.galarza.factus.detail.entity.Detail;
import dev.galarza.factus.invoice.entity.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Detail Entity - Unit Tests")
class DetailEntityUnitTest {

    private Detail detail;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = Invoice.builder()
                .id(1L)
                .numeroFactura("TEST-001")
                .build();

        detail = Detail.builder()
                .id(1L)
                .invoice(invoice)
                .descripcion("Producto Test")
                .cantidad(1)
                .precioUnitario(new BigDecimal("100.00"))
                .descuento(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .build();
    }

    @Test
    @DisplayName("Test 1: Calcular subtotal sin descuento")
    void testCalculateSubtotal_NoDiscount() {
        // Given
        detail.setCantidad(5);
        detail.setPrecioUnitario(new BigDecimal("50.00"));
        detail.setDescuento(BigDecimal.ZERO);

        // When
        detail.calcularSubtotal();

        // Then
        assertThat(detail.getSubtotal()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    @DisplayName("Test 2: Calcular subtotal con descuento")
    void testCalculateSubtotal_WithDiscount() {
        // Given
        detail.setCantidad(3);
        detail.setPrecioUnitario(new BigDecimal("100.00"));
        detail.setDescuento(new BigDecimal("50.00"));

        // When
        detail.calcularSubtotal();

        // Then
        // (3 * 100) - 50 = 250
        assertThat(detail.getSubtotal()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    @DisplayName("Test 3: Calcular subtotal con cantidad grande")
    void testCalculateSubtotal_LargeQuantity() {
        // Given
        detail.setCantidad(1000);
        detail.setPrecioUnitario(new BigDecimal("25.50"));
        detail.setDescuento(new BigDecimal("1000.00"));

        // When
        detail.calcularSubtotal();

        // Then
        // (1000 * 25.50) - 1000 = 24500
        assertThat(detail.getSubtotal()).isEqualByComparingTo(new BigDecimal("24500.00"));
    }

    @Test
    @DisplayName("Test 4: Calcular subtotal con precio unitario decimal")
    void testCalculateSubtotal_DecimalPrice() {
        // Given
        detail.setCantidad(7);
        detail.setPrecioUnitario(new BigDecimal("12.75"));
        detail.setDescuento(new BigDecimal("5.25"));

        // When
        detail.calcularSubtotal();

        // Then
        // (7 * 12.75) - 5.25 = 84.00
        assertThat(detail.getSubtotal()).isEqualByComparingTo(new BigDecimal("84.00"));
    }

    @Test
    @DisplayName("Test 5: Calcular subtotal con descuento mayor que subtotal sin descuento")
    void testCalculateSubtotal_DiscountGreaterThanSubtotal() {
        // Given
        detail.setCantidad(2);
        detail.setPrecioUnitario(new BigDecimal("50.00"));
        detail.setDescuento(new BigDecimal("150.00"));

        // When
        detail.calcularSubtotal();

        // Then
        // (2 * 50) - 150 = -50 (resultado negativo)
        assertThat(detail.getSubtotal()).isEqualByComparingTo(new BigDecimal("-50.00"));
    }

    @Test
    @DisplayName("Test 6: Validar que cantidad mínima es 1")
    void testValidateMinimumQuantity() {
        // Given
        detail.setCantidad(1);
        detail.setPrecioUnitario(new BigDecimal("100.00"));

        // When
        detail.calcularSubtotal();

        // Then
        assertThat(detail.getCantidad()).isGreaterThanOrEqualTo(1);
        assertThat(detail.getSubtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Test 7: Validar precio unitario positivo")
    void testValidatePricePositive() {
        // Given
        detail.setCantidad(5);
        detail.setPrecioUnitario(new BigDecimal("0.01")); // Precio mínimo

        // When
        detail.calcularSubtotal();

        // Then
        assertThat(detail.getPrecioUnitario()).isGreaterThan(BigDecimal.ZERO);
        assertThat(detail.getSubtotal()).isEqualByComparingTo(new BigDecimal("0.05"));
    }

    @Test
    @DisplayName("Test 8: Calcular subtotal con valores muy pequeños")
    void testCalculateSubtotal_VerySmallValues() {
        // Given
        detail.setCantidad(1);
        detail.setPrecioUnitario(new BigDecimal("0.01"));
        detail.setDescuento(new BigDecimal("0.005"));

        // When
        detail.calcularSubtotal();

        // Then
        assertThat(detail.getSubtotal()).isEqualByComparingTo(new BigDecimal("0.005"));
    }

    @Test
    @DisplayName("Test 9: Builder pattern crea detail correctamente")
    void testBuilderPattern_CreatesDetailCorrectly() {
        // When
        Detail newDetail = Detail.builder()
                .id(2L)
                .invoice(invoice)
                .descripcion("Servicio Premium")
                .cantidad(10)
                .precioUnitario(new BigDecimal("99.99"))
                .descuento(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("899.90"))
                .unidadMedida("SERV")
                .codigoProducto("PREM-001")
                .build();

        // Then
        assertThat(newDetail.getId()).isEqualTo(2L);
        assertThat(newDetail.getInvoice()).isEqualTo(invoice);
        assertThat(newDetail.getDescripcion()).isEqualTo("Servicio Premium");
        assertThat(newDetail.getCantidad()).isEqualTo(10);
        assertThat(newDetail.getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(newDetail.getUnidadMedida()).isEqualTo("SERV");
        assertThat(newDetail.getCodigoProducto()).isEqualTo("PREM-001");
    }

    @Test
    @DisplayName("Test 10: Validar campos opcionales pueden ser null")
    void testOptionalFields_CanBeNull() {
        // Given
        Detail detailWithOptionals = Detail.builder()
                .descripcion("Producto Básico")
                .cantidad(1)
                .precioUnitario(new BigDecimal("50.00"))
                .descuento(BigDecimal.ZERO)
                .unidadMedida(null)
                .codigoProducto(null)
                .build();

        // Then
        assertThat(detailWithOptionals.getUnidadMedida()).isNull();
        assertThat(detailWithOptionals.getCodigoProducto()).isNull();
        assertThat(detailWithOptionals.getDescripcion()).isNotNull();
        assertThat(detailWithOptionals.getCantidad()).isNotNull();
        assertThat(detailWithOptionals.getPrecioUnitario()).isNotNull();
    }
}

