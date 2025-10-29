package dev.galarza.factus.detail.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.galarza.factus.client.entity.Client;
import dev.galarza.factus.client.repository.ClientRepository;
import dev.galarza.factus.detail.dto.DetailRequestDTO;
import dev.galarza.factus.detail.entity.Detail;
import dev.galarza.factus.detail.repository.DetailRepository;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Detail Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DetailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DetailRepository detailRepository;

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
        detailRepository.deleteAll();
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
                .nombre("Test")
                .apellido("Client")
                .nit(1234567890L)
                .email("test@client.com")
                .activo(true)
                .build();
        testClient = clientRepository.save(testClient);
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Crear detalle a través de factura persiste correctamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateDetail_ThroughInvoice_Persists() throws Exception {
        // Given
        DetailRequestDTO detailRequest = DetailRequestDTO.builder()
                .descripcion("Laptop Dell XPS 15")
                .cantidad(2)
                .precioUnitario(new BigDecimal("1500.00"))
                .descuento(new BigDecimal("100.00"))
                .unidadMedida("UND")
                .codigoProducto("DELL-XPS-15")
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detailRequest))
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.detalles[0].descripcion").value("Laptop Dell XPS 15"))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(2))
                .andExpect(jsonPath("$.detalles[0].codigoProducto").value("DELL-XPS-15"))
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO invoiceResponse = objectMapper.readValue(response, InvoiceResponseDTO.class);

        List<Detail> details = detailRepository.findByInvoiceId(invoiceResponse.getId());
        assertThat(details).hasSize(1);
        assertThat(details.get(0).getDescripcion()).isEqualTo("Laptop Dell XPS 15");
        assertThat(details.get(0).getCantidad()).isEqualTo(2);
        assertThat(details.get(0).getCodigoProducto()).isEqualTo("DELL-XPS-15");
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Múltiples detalles con diferentes unidades de medida")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateMultipleDetails_DifferentUnits() throws Exception {
        // Given
        DetailRequestDTO detail1 = DetailRequestDTO.builder()
                .descripcion("Cemento Portland")
                .cantidad(50)
                .precioUnitario(new BigDecimal("45.00"))
                .unidadMedida("KG")
                .codigoProducto("CEM-001")
                .build();

        DetailRequestDTO detail2 = DetailRequestDTO.builder()
                .descripcion("Pintura Látex")
                .cantidad(20)
                .precioUnitario(new BigDecimal("35.50"))
                .unidadMedida("LTS")
                .codigoProducto("PINT-002")
                .build();

        DetailRequestDTO detail3 = DetailRequestDTO.builder()
                .descripcion("Servicio de Instalación")
                .cantidad(1)
                .precioUnitario(new BigDecimal("500.00"))
                .unidadMedida("SERV")
                .codigoProducto("SERV-INST")
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail1, detail2, detail3))
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.detalles").isArray())
                .andExpect(jsonPath("$.detalles.length()").value(3))
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO invoiceResponse = objectMapper.readValue(response, InvoiceResponseDTO.class);

        List<Detail> details = detailRepository.findByInvoiceId(invoiceResponse.getId());
        assertThat(details).hasSize(3);
        assertThat(details).extracting(Detail::getUnidadMedida)
                .containsExactlyInAnyOrder("KG", "LTS", "SERV");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Cálculo de subtotal con descuento en detalles")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDetailSubtotal_WithDiscount_CalculatesCorrectly() throws Exception {
        // Given
        DetailRequestDTO detailWithDiscount = DetailRequestDTO.builder()
                .descripcion("Producto con Descuento")
                .cantidad(5)
                .precioUnitario(new BigDecimal("100.00"))
                .descuento(new BigDecimal("50.00"))
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detailWithDiscount))
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.detalles[0].subtotal").value(450.00)) // (5*100) - 50
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO invoiceResponse = objectMapper.readValue(response, InvoiceResponseDTO.class);

        List<Detail> details = detailRepository.findByInvoiceId(invoiceResponse.getId());
        Detail savedDetail = details.get(0);
        assertThat(savedDetail.getSubtotal()).isEqualByComparingTo(new BigDecimal("450.00"));
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Detalles con precios decimales precisos")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDetailPrecision_DecimalPrices() throws Exception {
        // Given
        DetailRequestDTO detail = DetailRequestDTO.builder()
                .descripcion("Producto con Precio Decimal")
                .cantidad(3)
                .precioUnitario(new BigDecimal("33.33"))
                .descuento(BigDecimal.ZERO)
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail))
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO invoiceResponse = objectMapper.readValue(response, InvoiceResponseDTO.class);

        List<Detail> details = detailRepository.findByInvoiceId(invoiceResponse.getId());
        Detail savedDetail = details.get(0);
        // 3 * 33.33 = 99.99
        assertThat(savedDetail.getSubtotal()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Validación de cantidad mínima en detalle")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDetailValidation_MinimumQuantity() throws Exception {
        // Given
        DetailRequestDTO invalidDetail = DetailRequestDTO.builder()
                .descripcion("Producto Inválido")
                .cantidad(0) // Cantidad inválida
                .precioUnitario(new BigDecimal("100.00"))
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(invalidDetail))
                .build();

        // When & Then
        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Validación de precio unitario mínimo")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDetailValidation_MinimumPrice() throws Exception {
        // Given
        DetailRequestDTO invalidDetail = DetailRequestDTO.builder()
                .descripcion("Producto Inválido")
                .cantidad(1)
                .precioUnitario(new BigDecimal("0.00")) // Precio inválido
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(invalidDetail))
                .build();

        // When & Then
        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Detalles con códigos de producto únicos")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDetails_WithProductCodes() throws Exception {
        // Given
        DetailRequestDTO detail1 = DetailRequestDTO.builder()
                .descripcion("Producto A")
                .cantidad(1)
                .precioUnitario(new BigDecimal("100.00"))
                .codigoProducto("PROD-A-001")
                .build();

        DetailRequestDTO detail2 = DetailRequestDTO.builder()
                .descripcion("Producto B")
                .cantidad(1)
                .precioUnitario(new BigDecimal("200.00"))
                .codigoProducto("PROD-B-002")
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail1, detail2))
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO invoiceResponse = objectMapper.readValue(response, InvoiceResponseDTO.class);

        List<Detail> details = detailRepository.findByInvoiceId(invoiceResponse.getId());
        assertThat(details).hasSize(2);
        assertThat(details).extracting(Detail::getCodigoProducto)
                .containsExactlyInAnyOrder("PROD-A-001", "PROD-B-002");
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Detalle con descripción larga")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testDetail_LongDescription() throws Exception {
        // Given
        String longDescription = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.";

        DetailRequestDTO detail = DetailRequestDTO.builder()
                .descripcion(longDescription)
                .cantidad(1)
                .precioUnitario(new BigDecimal("100.00"))
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail))
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO invoiceResponse = objectMapper.readValue(response, InvoiceResponseDTO.class);

        List<Detail> details = detailRepository.findByInvoiceId(invoiceResponse.getId());
        assertThat(details.get(0).getDescripcion()).isEqualTo(longDescription);
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Relación bidireccional entre Invoice y Detail")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testBidirectionalRelationship_InvoiceDetail() throws Exception {
        // Given
        DetailRequestDTO detail = DetailRequestDTO.builder()
                .descripcion("Test Relationship")
                .cantidad(1)
                .precioUnitario(new BigDecimal("100.00"))
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail))
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        InvoiceResponseDTO invoiceResponse = objectMapper.readValue(response, InvoiceResponseDTO.class);

        Invoice savedInvoice = invoiceRepository.findById(invoiceResponse.getId()).orElseThrow();
        List<Detail> details = detailRepository.findByInvoiceId(invoiceResponse.getId());

        assertThat(details).hasSize(1);
        assertThat(details.get(0).getInvoice()).isNotNull();
        assertThat(details.get(0).getInvoice().getId()).isEqualTo(savedInvoice.getId());
        assertThat(savedInvoice.getDetalles()).hasSize(1);
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Cascada de eliminación - eliminar factura elimina detalles")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testCascadeDelete_InvoiceDeletesDetails() throws Exception {
        // Given - Crear factura con detalles
        DetailRequestDTO detail1 = DetailRequestDTO.builder()
                .descripcion("Detail 1")
                .cantidad(1)
                .precioUnitario(new BigDecimal("100.00"))
                .build();

        DetailRequestDTO detail2 = DetailRequestDTO.builder()
                .descripcion("Detail 2")
                .cantidad(2)
                .precioUnitario(new BigDecimal("50.00"))
                .build();

        InvoiceRequestDTO invoiceRequest = InvoiceRequestDTO.builder()
                .serie("FAC")
                .fechaEmision(LocalDate.now())
                .clientId(testClient.getId())
                .tipoComprobante(TipoComprobante.FACTURA)
                .detalles(Arrays.asList(detail1, detail2))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        InvoiceResponseDTO created = objectMapper.readValue(createResponse, InvoiceResponseDTO.class);

        // Verificar que los detalles existen
        List<Detail> detailsBeforeDelete = detailRepository.findByInvoiceId(created.getId());
        assertThat(detailsBeforeDelete).hasSize(2);

        // When - Eliminar factura
        mockMvc.perform(delete("/api/invoices/" + created.getId()))
                .andExpect(status().isNoContent());

        // Then - Verificar que los detalles también se eliminaron
        List<Detail> detailsAfterDelete = detailRepository.findByInvoiceId(created.getId());
        assertThat(detailsAfterDelete).isEmpty();
        assertThat(invoiceRepository.existsById(created.getId())).isFalse();
    }
}

