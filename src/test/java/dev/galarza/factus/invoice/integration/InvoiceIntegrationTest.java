package dev.galarza.factus.invoice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.galarza.factus.client.entity.Client;
import dev.galarza.factus.client.repository.ClientRepository;
import dev.galarza.factus.detail.dto.DetailRequestDTO;
import dev.galarza.factus.invoice.dto.InvoiceRequestDTO;
import dev.galarza.factus.invoice.dto.InvoiceResponseDTO;
import dev.galarza.factus.invoice.entity.Invoice;
import dev.galarza.factus.invoice.entity.InvoiceStatus;
import dev.galarza.factus.invoice.entity.TipoComprobante;
import dev.galarza.factus.invoice.repository.InvoiceRepository;
import dev.galarza.factus.security.entity.Role;
import dev.galarza.factus.security.entity.RoleName;
import dev.galarza.factus.security.entity.User;
import dev.galarza.factus.security.repository.RoleRepository;
import dev.galarza.factus.security.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Invoice Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InvoiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Client testClient;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
        clientRepository.deleteAll();
        setupUserAndRoles();
        setupTestClient();
    }

    private void setupUserAndRoles() {
        if (!roleRepository.existsByNombre(RoleName.ROLE_USER)) {
            Role userRole = Role.builder()
                    .nombre(RoleName.ROLE_USER)
                    .descripcion("Usuario estándar")
                    .build();
            roleRepository.save(userRole);
        }

        if (!userRepository.existsByUsername("testuser")) {
            Role userRole = roleRepository.findByNombre(RoleName.ROLE_USER).orElseThrow();
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);

            User user = User.builder()
                    .username("testuser")
                    .password(passwordEncoder.encode("test123"))
                    .email("test@example.com")
                    .nombre("Test")
                    .apellido("User")
                    .roles(roles)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            userRepository.save(user);
        }
    }

    private void setupTestClient() {
        testClient = Client.builder()
                .nombre("Carlos")
                .apellido("Mendoza")
                .nit(1234567890L)
                .email("carlos@example.com")
                .activo(true)
                .build();
        testClient = clientRepository.save(testClient);
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Crear factura con cálculo automático de IVA 13% e IT 3%")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateInvoice_CalculatesTaxesAutomatically() throws Exception {
        // Given
        DetailRequestDTO detail = DetailRequestDTO.builder()
                .descripcion("Producto Test")
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .descuento(BigDecimal.ZERO)
                .build();

        InvoiceRequestDTO request = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail))
                .observaciones("Factura de prueba")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.subtotal").value(200.00))
                .andExpect(jsonPath("$.iva").value(26.00)) // 200 * 0.13
                .andExpect(jsonPath("$.it").value(6.00))   // 200 * 0.03
                .andExpect(jsonPath("$.total").value(232.00))
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO responseDTO = objectMapper.readValue(response, InvoiceResponseDTO.class);

        Invoice savedInvoice = invoiceRepository.findById(responseDTO.getId()).orElseThrow();
        assertThat(savedInvoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(savedInvoice.getIva()).isEqualByComparingTo(new BigDecimal("26.00"));
        assertThat(savedInvoice.getIt()).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(savedInvoice.getTotal()).isEqualByComparingTo(new BigDecimal("232.00"));
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Crear factura con múltiples detalles y descuentos")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateInvoice_MultipleDetailsWithDiscounts() throws Exception {
        // Given
        DetailRequestDTO detail1 = DetailRequestDTO.builder()
                .descripcion("Producto A")
                .cantidad(3)
                .precioUnitario(new BigDecimal("50.00"))
                .descuento(new BigDecimal("15.00"))
                .build();

        DetailRequestDTO detail2 = DetailRequestDTO.builder()
                .descripcion("Producto B")
                .cantidad(2)
                .precioUnitario(new BigDecimal("100.00"))
                .descuento(BigDecimal.ZERO)
                .build();

        InvoiceRequestDTO request = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail1, detail2))
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.detalles").isArray())
                .andExpect(jsonPath("$.detalles.length()").value(2))
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO responseDTO = objectMapper.readValue(response, InvoiceResponseDTO.class);

        // Subtotal: (3*50-15) + (2*100) = 135 + 200 = 335
        // IVA: 335 * 0.13 = 43.55
        // IT: 335 * 0.03 = 10.05
        // Total: 335 + 43.55 + 10.05 = 388.60
        assertThat(responseDTO.getSubtotal()).isEqualByComparingTo(new BigDecimal("335.00"));
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Generar número de factura secuencial por serie")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateInvoice_GeneratesSequentialNumbers() throws Exception {
        // Given
        DetailRequestDTO detail = DetailRequestDTO.builder()
                .descripcion("Producto")
                .cantidad(1)
                .precioUnitario(new BigDecimal("100.00"))
                .build();

        InvoiceRequestDTO request = InvoiceRequestDTO.builder()
                .serie("TEST")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail))
                .build();

        // When - Crear 3 facturas
        MvcResult result1 = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroFactura").value("TEST-00000001"))
                .andReturn();

        MvcResult result2 = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroFactura").value("TEST-00000002"))
                .andReturn();

        MvcResult result3 = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroFactura").value("TEST-00000003"))
                .andReturn();

        // Then
        assertThat(invoiceRepository.count()).isEqualTo(3);
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Emitir factura cambia estado de BORRADOR a EMITIDA")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testEmitInvoice_ChangesStatusCorrectly() throws Exception {
        // Given
        Invoice invoice = createTestInvoice(InvoiceStatus.BORRADOR);

        // When
        mockMvc.perform(patch("/api/invoices/" + invoice.getId() + "/emit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EMITIDA"));

        // Then
        Invoice updatedInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(updatedInvoice.getEstado()).isEqualTo(InvoiceStatus.EMITIDA);
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Anular factura emitida funciona correctamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCancelInvoice_EmittedInvoice() throws Exception {
        // Given
        Invoice invoice = createTestInvoice(InvoiceStatus.EMITIDA);

        // When
        mockMvc.perform(patch("/api/invoices/" + invoice.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADA"));

        // Then
        Invoice cancelledInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(cancelledInvoice.getEstado()).isEqualTo(InvoiceStatus.ANULADA);
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: No se puede anular factura pagada")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCancelInvoice_PaidInvoice_ThrowsError() throws Exception {
        // Given
        Invoice invoice = createTestInvoice(InvoiceStatus.PAGADA);

        // When & Then
        mockMvc.perform(patch("/api/invoices/" + invoice.getId() + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("pagada")));

        Invoice unchangedInvoice = invoiceRepository.findById(invoice.getId()).orElseThrow();
        assertThat(unchangedInvoice.getEstado()).isEqualTo(InvoiceStatus.PAGADA);
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Buscar facturas por cliente retorna todas sus facturas")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetInvoicesByClient_ReturnsAllClientInvoices() throws Exception {
        // Given - Crear múltiples facturas para el mismo cliente
        createTestInvoice(InvoiceStatus.BORRADOR);
        createTestInvoice(InvoiceStatus.EMITIDA);
        createTestInvoice(InvoiceStatus.PAGADA);

        // When
        MvcResult result = mockMvc.perform(get("/api/invoices/client/" + testClient.getId()))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO[] invoices = objectMapper.readValue(response, InvoiceResponseDTO[].class);

        assertThat(invoices).hasSize(3);
        assertThat(invoices).allMatch(inv -> inv.getClient().getId().equals(testClient.getId()));
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Filtrar facturas por estado funciona correctamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetInvoicesByStatus_FiltersCorrectly() throws Exception {
        // Given
        createTestInvoice(InvoiceStatus.BORRADOR);
        createTestInvoice(InvoiceStatus.BORRADOR);
        createTestInvoice(InvoiceStatus.EMITIDA);
        createTestInvoice(InvoiceStatus.PAGADA);

        // When
        MvcResult result = mockMvc.perform(get("/api/invoices/status/BORRADOR"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO[] invoices = objectMapper.readValue(response, InvoiceResponseDTO[].class);

        assertThat(invoices).hasSize(2);
        assertThat(invoices).allMatch(inv -> inv.getEstado() == InvoiceStatus.BORRADOR);
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Buscar facturas por rango de fechas")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testGetInvoicesByDateRange_FiltersCorrectly() throws Exception {
        // Given
        Invoice invoice1 = createTestInvoiceWithDate(LocalDate.of(2025, 10, 15));
        Invoice invoice2 = createTestInvoiceWithDate(LocalDate.of(2025, 10, 20));
        Invoice invoice3 = createTestInvoiceWithDate(LocalDate.of(2025, 11, 5)); // Fuera del rango

        // When
        MvcResult result = mockMvc.perform(get("/api/invoices/date-range")
                        .param("startDate", "2025-10-01")
                        .param("endDate", "2025-10-31"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO[] invoices = objectMapper.readValue(response, InvoiceResponseDTO[].class);

        assertThat(invoices).hasSize(2);
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Ciclo completo CRUD de factura con validaciones")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testCompleteInvoiceCRUD_WithValidations() throws Exception {
        // Create
        DetailRequestDTO detail = DetailRequestDTO.builder()
                .descripcion("Servicio Profesional")
                .cantidad(10)
                .precioUnitario(new BigDecimal("50.00"))
                .build();

        InvoiceRequestDTO createRequest = InvoiceRequestDTO.builder()
                .serie("SRV")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail))
                .observaciones("Servicio de consultoría")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        InvoiceResponseDTO created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                InvoiceResponseDTO.class
        );

        // Read
        mockMvc.perform(get("/api/invoices/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroFactura").value(created.getNumeroFactura()))
                .andExpect(jsonPath("$.estado").value("BORRADOR"));

        // Emit
        mockMvc.perform(patch("/api/invoices/" + created.getId() + "/emit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EMITIDA"));

        // Update Status to PAGADA
        mockMvc.perform(patch("/api/invoices/" + created.getId() + "/status")
                        .param("status", "PAGADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"));

        // Try to delete (should fail - not BORRADOR)
        mockMvc.perform(delete("/api/invoices/" + created.getId()))
                .andExpect(status().isBadRequest());

        // Cancel
        mockMvc.perform(patch("/api/invoices/" + created.getId() + "/cancel"))
                .andExpect(status().isBadRequest()); // Can't cancel paid invoice

        // Verify final state
        Invoice finalInvoice = invoiceRepository.findById(created.getId()).orElseThrow();
        assertThat(finalInvoice.getEstado()).isEqualTo(InvoiceStatus.PAGADA);
    }

    // Helper methods
    private Invoice createTestInvoice(InvoiceStatus status) {
        Invoice invoice = Invoice.builder()
                .numeroFactura("TEST-" + System.currentTimeMillis())
                .serie("TEST")
                .fechaEmision(LocalDate.now())
                .client(testClient)
                .subtotal(new BigDecimal("100.00"))
                .iva(new BigDecimal("13.00"))
                .it(new BigDecimal("3.00"))
                .total(new BigDecimal("116.00"))
                .estado(status)
                .tipoComprobante(TipoComprobante.FACTURA)
                .build();
        return invoiceRepository.save(invoice);
    }

    private Invoice createTestInvoiceWithDate(LocalDate date) {
        Invoice invoice = Invoice.builder()
                .numeroFactura("DATE-" + System.currentTimeMillis())
                .serie("DATE")
                .fechaEmision(date)
                .client(testClient)
                .subtotal(new BigDecimal("100.00"))
                .iva(new BigDecimal("13.00"))
                .it(new BigDecimal("3.00"))
                .total(new BigDecimal("116.00"))
                .estado(InvoiceStatus.EMITIDA)
                .tipoComprobante(TipoComprobante.FACTURA)
                .build();
        return invoiceRepository.save(invoice);
    }
}

