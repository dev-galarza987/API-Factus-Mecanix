package dev.galarza.factus.invoice.unit;

import dev.galarza.factus.client.entity.Client;
import dev.galarza.factus.client.repository.ClientRepository;
import dev.galarza.factus.detail.dto.DetailRequestDTO;
import dev.galarza.factus.detail.entity.Detail;
import dev.galarza.factus.detail.mapper.DetailMapper;
import dev.galarza.factus.exception.BadRequestException;
import dev.galarza.factus.exception.ResourceNotFoundException;
import dev.galarza.factus.invoice.dto.InvoiceRequestDTO;
import dev.galarza.factus.invoice.dto.InvoiceResponseDTO;
import dev.galarza.factus.invoice.entity.Invoice;
import dev.galarza.factus.invoice.entity.InvoiceStatus;
import dev.galarza.factus.invoice.entity.TipoComprobante;
import dev.galarza.factus.invoice.mapper.InvoiceMapper;
import dev.galarza.factus.invoice.repository.InvoiceRepository;
import dev.galarza.factus.invoice.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Invoice Service - Unit Tests")
class InvoiceServiceUnitTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private DetailMapper detailMapper;

    @InjectMocks
    private InvoiceService invoiceService;

    private Client client;
    private Invoice invoice;
    private InvoiceRequestDTO requestDTO;
    private InvoiceResponseDTO responseDTO;
    private DetailRequestDTO detailRequestDTO;
    private Detail detail;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .nit(1234567890L)
                .activo(true)
                .build();

        detail = Detail.builder()
                .id(1L)
                .descripcion("Producto A")
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .descuento(BigDecimal.ZERO)
                .subtotal(new BigDecimal("200.00"))
                .build();

        invoice = Invoice.builder()
                .id(1L)
                .numeroFactura("FAC-00000001")
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .client(client)
                .subtotal(new BigDecimal("200.00"))
                .iva(new BigDecimal("26.00"))
                .it(new BigDecimal("6.00"))
                .total(new BigDecimal("232.00"))
                .estado(InvoiceStatus.BORRADOR)
                .tipoComprobante(TipoComprobante.FACTURA)
                .build();

        detailRequestDTO = DetailRequestDTO.builder()
                .descripcion("Producto A")
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .descuento(BigDecimal.ZERO)
                .build();

        requestDTO = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(1L)
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detailRequestDTO))
                .build();
    }

    @Test
    @DisplayName("Test 1: Crear factura con cálculo automático de impuestos (IVA 13%, IT 3%)")
    void testCreateInvoice_CalculatesTaxesCorrectly() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(invoiceRepository.findLastNumberBySerie("FAC")).thenReturn(0);
        when(detailMapper.toEntity(any(DetailRequestDTO.class))).thenReturn(detail);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice inv = invocation.getArgument(0);
            inv.setId(1L);
            return inv;
        });
        when(invoiceMapper.toResponseDTO(any(Invoice.class))).thenReturn(responseDTO);

        // When
        InvoiceResponseDTO result = invoiceService.createInvoice(requestDTO);

        // Then
        verify(invoiceRepository).save(argThat(inv ->
            inv.getSubtotal().compareTo(new BigDecimal("200.00")) == 0 &&
            inv.getIva().compareTo(new BigDecimal("26.00")) == 0 && // 200 * 0.13
            inv.getIt().compareTo(new BigDecimal("6.00")) == 0 && // 200 * 0.03
            inv.getTotal().compareTo(new BigDecimal("232.00")) == 0
        ));
    }

    @Test
    @DisplayName("Test 2: Generar número de factura secuencial por serie")
    void testCreateInvoice_GeneratesSequentialNumber() {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(invoiceRepository.findLastNumberBySerie("FAC")).thenReturn(42);
        when(detailMapper.toEntity(any(DetailRequestDTO.class))).thenReturn(detail);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceMapper.toResponseDTO(any(Invoice.class))).thenReturn(responseDTO);

        // When
        invoiceService.createInvoice(requestDTO);

        // Then
        verify(invoiceRepository).save(argThat(inv ->
            inv.getNumeroFactura().equals("FAC-00000043")
        ));
    }

    @Test
    @DisplayName("Test 3: Crear factura con cliente inexistente lanza ResourceNotFoundException")
    void testCreateInvoice_ClientNotFound_ThrowsException() {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());
        requestDTO.setClientId(999L);

        // When & Then
        assertThatThrownBy(() -> invoiceService.createInvoice(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente")
                .hasMessageContaining("999");

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 4: Crear factura con múltiples detalles calcula subtotales correctamente")
    void testCreateInvoice_MultipleDetails_CalculatesCorrectly() {
        // Given
        Detail detail1 = Detail.builder()
                .descripcion("Producto A")
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .descuento(BigDecimal.ZERO)
                .subtotal(new BigDecimal("200.00"))
                .build();

        Detail detail2 = Detail.builder()
                .descripcion("Producto B")
                .cantidad(1)
                .precioUnitario(new BigDecimal("150.00"))
                .descuento(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("140.00"))
                .build();

        DetailRequestDTO detailDTO1 = DetailRequestDTO.builder()
                .descripcion("Producto A")
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .build();

        DetailRequestDTO detailDTO2 = DetailRequestDTO.builder()
                .descripcion("Producto B")
                .cantidad(1)
                .precioUnitario(new BigDecimal("150.00"))
                .descuento(new BigDecimal("10.00"))
                .build();

        requestDTO.setDetalles(Arrays.asList(detailDTO1, detailDTO2));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(invoiceRepository.findLastNumberBySerie("FAC")).thenReturn(0);
        when(detailMapper.toEntity(detailDTO1)).thenReturn(detail1);
        when(detailMapper.toEntity(detailDTO2)).thenReturn(detail2);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceMapper.toResponseDTO(any(Invoice.class))).thenReturn(responseDTO);

        // When
        invoiceService.createInvoice(requestDTO);

        // Then
        verify(invoiceRepository).save(argThat(inv -> {
            BigDecimal expectedSubtotal = new BigDecimal("340.00"); // 200 + 140
            BigDecimal expectedIva = new BigDecimal("44.20"); // 340 * 0.13
            BigDecimal expectedIt = new BigDecimal("10.20"); // 340 * 0.03
            BigDecimal expectedTotal = new BigDecimal("394.40"); // 340 + 44.20 + 10.20

            return inv.getSubtotal().compareTo(expectedSubtotal) == 0 &&
                   inv.getIva().compareTo(expectedIva) == 0 &&
                   inv.getIt().compareTo(expectedIt) == 0 &&
                   inv.getTotal().compareTo(expectedTotal) == 0;
        }));
    }

    @Test
    @DisplayName("Test 5: Emitir factura solo funciona con estado BORRADOR")
    void testEmitInvoice_OnlyWorksWithDraftStatus() {
        // Given - Factura en BORRADOR
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toResponseDTO(any(Invoice.class))).thenReturn(responseDTO);

        // When
        invoiceService.emitInvoice(1L);

        // Then
        verify(invoiceRepository).save(argThat(inv ->
            inv.getEstado() == InvoiceStatus.EMITIDA
        ));
    }

    @Test
    @DisplayName("Test 6: Emitir factura ya emitida lanza BadRequestException")
    void testEmitInvoice_AlreadyEmitted_ThrowsException() {
        // Given
        invoice.setEstado(InvoiceStatus.EMITIDA);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When & Then
        assertThatThrownBy(() -> invoiceService.emitInvoice(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("BORRADOR");

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 7: Anular factura pagada lanza BadRequestException")
    void testCancelInvoice_PaidInvoice_ThrowsException() {
        // Given
        invoice.setEstado(InvoiceStatus.PAGADA);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When & Then
        assertThatThrownBy(() -> invoiceService.cancelInvoice(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("pagada");

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 8: Buscar facturas por rango de fechas")
    void testGetInvoicesByDateRange_FiltersCorrectly() {
        // Given
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);

        Invoice invoice1 = Invoice.builder()
                .id(1L)
                .fechaEmision(LocalDate.of(2025, 10, 15))
                .build();
        Invoice invoice2 = Invoice.builder()
                .id(2L)
                .fechaEmision(LocalDate.of(2025, 10, 20))
                .build();

        when(invoiceRepository.findByDateRange(startDate, endDate))
                .thenReturn(Arrays.asList(invoice1, invoice2));
        when(invoiceMapper.toResponseDTO(any(Invoice.class)))
                .thenReturn(responseDTO);

        // When
        List<InvoiceResponseDTO> result = invoiceService.getInvoicesByDateRange(startDate, endDate);

        // Then
        assertThat(result).hasSize(2);
        verify(invoiceRepository).findByDateRange(startDate, endDate);
        verify(invoiceMapper, times(2)).toResponseDTO(any(Invoice.class));
    }

    @Test
    @DisplayName("Test 9: Validar transiciones de estado permitidas")
    void testUpdateInvoiceStatus_ValidatesTransitions() {
        // Given - BORRADOR puede pasar a EMITIDA
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toResponseDTO(any(Invoice.class))).thenReturn(responseDTO);

        // When
        invoiceService.updateInvoiceStatus(1L, InvoiceStatus.EMITIDA);

        // Then
        verify(invoiceRepository).save(argThat(inv ->
            inv.getEstado() == InvoiceStatus.EMITIDA
        ));
    }

    @Test
    @DisplayName("Test 10: No se puede cambiar estado de factura anulada")
    void testUpdateInvoiceStatus_CancelledInvoice_ThrowsException() {
        // Given
        invoice.setEstado(InvoiceStatus.ANULADA);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // When & Then
        assertThatThrownBy(() ->
            invoiceService.updateInvoiceStatus(1L, InvoiceStatus.EMITIDA))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("anulada");

        verify(invoiceRepository, never()).save(any());
    }
}

