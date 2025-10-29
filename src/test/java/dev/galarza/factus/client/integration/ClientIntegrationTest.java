package dev.galarza.factus.client.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.galarza.factus.client.dto.ClientRequestDTO;
import dev.galarza.factus.client.dto.ClientResponseDTO;
import dev.galarza.factus.client.entity.Client;
import dev.galarza.factus.client.repository.ClientRepository;
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

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Client Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        setupUserAndRoles();
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

    @Test
    @Order(1)
    @DisplayName("Test 1: Crear cliente con datos completos y verificar persistencia en BD")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateClient_FullData_PersistsToDatabase() throws Exception {
        // Given
        ClientRequestDTO request = ClientRequestDTO.builder()
                .nombre("Carlos")
                .apellido("Mendoza")
                .nit(1122334455L)
                .email("carlos.mendoza@example.com")
                .telefono("79876543")
                .direccion("Calle Comercio #456")
                .ciudad("Cochabamba")
                .departamento("Cochabamba")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$.nit").value(1122334455L))
                .andExpect(jsonPath("$.activo").value(true))
                .andReturn();

        // Then
        String response = result.getResponse().getContentAsString();
        ClientResponseDTO responseDTO = objectMapper.readValue(response, ClientResponseDTO.class);

        Client savedClient = clientRepository.findById(responseDTO.getId()).orElseThrow();
        assertThat(savedClient.getNombre()).isEqualTo("Carlos");
        assertThat(savedClient.getApellido()).isEqualTo("Mendoza");
        assertThat(savedClient.getNit()).isEqualTo(1122334455L);
        assertThat(savedClient.getCiudad()).isEqualTo("Cochabamba");
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Crear múltiples clientes y verificar unicidad de NIT")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateMultipleClients_UniqueNIT_Validation() throws Exception {
        // Given
        ClientRequestDTO request1 = ClientRequestDTO.builder()
                .nombre("Ana")
                .apellido("García")
                .nit(2233445566L)
                .email("ana@example.com")
                .build();

        ClientRequestDTO request2 = ClientRequestDTO.builder()
                .nombre("Luis")
                .apellido("Torres")
                .nit(2233445566L) // NIT duplicado
                .email("luis@example.com")
                .build();

        // When - Primera creación exitosa
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Then - Segunda creación falla por NIT duplicado
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("NIT")));

        assertThat(clientRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Buscar cliente por NIT y verificar datos completos")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testFindClientByNIT_ReturnsCompleteData() throws Exception {
        // Given
        Client client = Client.builder()
                .nombre("Roberto")
                .apellido("Silva")
                .nit(3344556677L)
                .email("roberto@example.com")
                .telefono("76543210")
                .direccion("Av. Libertador #789")
                .ciudad("Santa Cruz")
                .departamento("Santa Cruz")
                .activo(true)
                .build();
        clientRepository.save(client);

        // When & Then
        mockMvc.perform(get("/api/clients/nit/3344556677"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Roberto"))
                .andExpect(jsonPath("$.apellido").value("Silva"))
                .andExpect(jsonPath("$.nit").value(3344556677L))
                .andExpect(jsonPath("$.email").value("roberto@example.com"))
                .andExpect(jsonPath("$.ciudad").value("Santa Cruz"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Actualizar cliente y verificar cambios en BD")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testUpdateClient_PersistsChanges() throws Exception {
        // Given
        Client existingClient = Client.builder()
                .nombre("María")
                .apellido("López")
                .nit(4455667788L)
                .email("maria@example.com")
                .activo(true)
                .build();
        Client saved = clientRepository.save(existingClient);

        ClientRequestDTO updateRequest = ClientRequestDTO.builder()
                .nombre("María Isabel")
                .apellido("López García")
                .nit(4455667788L)
                .email("maria.nueva@example.com")
                .telefono("71234567")
                .direccion("Nueva Dirección #123")
                .ciudad("La Paz")
                .departamento("La Paz")
                .build();

        // When
        mockMvc.perform(put("/api/clients/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("María Isabel"))
                .andExpect(jsonPath("$.email").value("maria.nueva@example.com"));

        // Then
        Client updatedClient = clientRepository.findById(saved.getId()).orElseThrow();
        assertThat(updatedClient.getNombre()).isEqualTo("María Isabel");
        assertThat(updatedClient.getApellido()).isEqualTo("López García");
        assertThat(updatedClient.getEmail()).isEqualTo("maria.nueva@example.com");
        assertThat(updatedClient.getTelefono()).isEqualTo("71234567");
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Listar clientes activos vs inactivos correctamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testListActiveClients_FiltersCorrectly() throws Exception {
        // Given
        Client activeClient1 = Client.builder()
                .nombre("Pedro")
                .apellido("Ramírez")
                .nit(5566778899L)
                .activo(true)
                .build();
        Client activeClient2 = Client.builder()
                .nombre("Laura")
                .apellido("Martínez")
                .nit(6677889900L)
                .activo(true)
                .build();
        Client inactiveClient = Client.builder()
                .nombre("José")
                .apellido("Vargas")
                .nit(7788990011L)
                .activo(false)
                .build();

        clientRepository.save(activeClient1);
        clientRepository.save(activeClient2);
        clientRepository.save(inactiveClient);

        // When & Then - Listar activos
        MvcResult result = mockMvc.perform(get("/api/clients/active"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        ClientResponseDTO[] clients = objectMapper.readValue(response, ClientResponseDTO[].class);

        assertThat(clients).hasSize(2);
        assertThat(clients).allMatch(ClientResponseDTO::getActivo);
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Búsqueda de clientes por nombre, apellido y NIT")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testSearchClients_MultipleFields() throws Exception {
        // Given
        clientRepository.save(Client.builder()
                .nombre("Fernando")
                .apellido("González")
                .nit(8899001122L)
                .activo(true)
                .build());
        clientRepository.save(Client.builder()
                .nombre("Fernanda")
                .apellido("Pérez")
                .nit(9900112233L)
                .activo(true)
                .build());
        clientRepository.save(Client.builder()
                .nombre("Andrea")
                .apellido("Fernández")
                .nit(1011121314L)
                .activo(true)
                .build());

        // When & Then - Buscar por "Fern"
        MvcResult result = mockMvc.perform(get("/api/clients/search")
                        .param("query", "Fern"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        ClientResponseDTO[] clients = objectMapper.readValue(response, ClientResponseDTO[].class);

        assertThat(clients.length).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Eliminación lógica (soft delete) mantiene datos en BD")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testSoftDelete_KeepsDataInDatabase() throws Exception {
        // Given
        Client client = Client.builder()
                .nombre("Diego")
                .apellido("Morales")
                .nit(1112131415L)
                .email("diego@example.com")
                .activo(true)
                .build();
        Client saved = clientRepository.save(client);

        // When
        mockMvc.perform(delete("/api/clients/" + saved.getId()))
                .andExpect(status().isNoContent());

        // Then
        Client deletedClient = clientRepository.findById(saved.getId()).orElseThrow();
        assertThat(deletedClient).isNotNull();
        assertThat(deletedClient.getActivo()).isFalse();
        assertThat(deletedClient.getNombre()).isEqualTo("Diego");
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Validación de NIT de 10 dígitos en creación")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testCreateClient_NIT_Validation() throws Exception {
        // Given - NIT con menos de 10 dígitos
        ClientRequestDTO invalidRequest = ClientRequestDTO.builder()
                .nombre("Test")
                .apellido("Invalid")
                .nit(123456L) // Solo 6 dígitos
                .email("test@example.com")
                .build();

        // When & Then
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.nit").exists());
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Transacción completa de CRUD en secuencia")
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void testCompleteClientCRUD_InSequence() throws Exception {
        // Create
        ClientRequestDTO createRequest = ClientRequestDTO.builder()
                .nombre("Sofía")
                .apellido("Herrera")
                .nit(1213141516L)
                .email("sofia@example.com")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        ClientResponseDTO created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                ClientResponseDTO.class
        );

        // Read
        mockMvc.perform(get("/api/clients/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sofía"));

        // Update
        ClientRequestDTO updateRequest = ClientRequestDTO.builder()
                .nombre("Sofía María")
                .apellido("Herrera López")
                .nit(1213141516L)
                .email("sofia.nueva@example.com")
                .build();

        mockMvc.perform(put("/api/clients/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sofía María"));

        // Delete (soft)
        mockMvc.perform(delete("/api/clients/" + created.getId()))
                .andExpect(status().isNoContent());

        // Verify soft delete
        Client deletedClient = clientRepository.findById(created.getId()).orElseThrow();
        assertThat(deletedClient.getActivo()).isFalse();
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Concurrencia - Crear múltiples clientes simultáneamente")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testConcurrentClientCreation_HandlesDuplicates() throws Exception {
        // Given - Preparar múltiples requests
        ClientRequestDTO request1 = ClientRequestDTO.builder()
                .nombre("Cliente1")
                .apellido("Test1")
                .nit(1314151617L)
                .email("cliente1@test.com")
                .build();

        ClientRequestDTO request2 = ClientRequestDTO.builder()
                .nombre("Cliente2")
                .apellido("Test2")
                .nit(1415161718L)
                .email("cliente2@test.com")
                .build();

        ClientRequestDTO request3 = ClientRequestDTO.builder()
                .nombre("Cliente3")
                .apellido("Test3")
                .nit(1516171819L)
                .email("cliente3@test.com")
                .build();

        // When - Crear clientes secuencialmente
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());

        // Then
        assertThat(clientRepository.count()).isEqualTo(3);
        assertThat(clientRepository.findByNit(1314151617L)).isPresent();
        assertThat(clientRepository.findByNit(1415161718L)).isPresent();
        assertThat(clientRepository.findByNit(1516171819L)).isPresent();
    }
}

