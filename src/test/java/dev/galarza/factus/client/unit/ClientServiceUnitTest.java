package dev.galarza.factus.client.unit;

import dev.galarza.factus.client.dto.ClientRequestDTO;
import dev.galarza.factus.client.dto.ClientResponseDTO;
import dev.galarza.factus.client.entity.Client;
import dev.galarza.factus.client.mapper.ClientMapper;
import dev.galarza.factus.client.repository.ClientRepository;
import dev.galarza.factus.client.service.ClientService;
import dev.galarza.factus.exception.BadRequestException;
import dev.galarza.factus.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Client Service - Unit Tests")
class ClientServiceUnitTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private ClientRequestDTO requestDTO;
    private ClientResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .nit(1234567890L)
                .email("juan@example.com")
                .telefono("77123456")
                .direccion("Av. Principal #123")
                .ciudad("La Paz")
                .departamento("La Paz")
                .activo(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        requestDTO = ClientRequestDTO.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .nit(1234567890L)
                .email("juan@example.com")
                .telefono("77123456")
                .direccion("Av. Principal #123")
                .ciudad("La Paz")
                .departamento("La Paz")
                .build();

        responseDTO = ClientResponseDTO.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .nit(1234567890L)
                .email("juan@example.com")
                .telefono("77123456")
                .direccion("Av. Principal #123")
                .ciudad("La Paz")
                .departamento("La Paz")
                .activo(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Test 1: Crear cliente exitosamente con todos los datos válidos")
    void testCreateClient_Success() {
        // Given
        when(clientRepository.existsByNit(anyLong())).thenReturn(false);
        when(clientRepository.existsByEmail(anyString())).thenReturn(false);
        when(clientMapper.toEntity(any(ClientRequestDTO.class))).thenReturn(client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toResponseDTO(any(Client.class))).thenReturn(responseDTO);

        // When
        ClientResponseDTO result = clientService.createClient(requestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNit()).isEqualTo(1234567890L);
        assertThat(result.getNombre()).isEqualTo("Juan");
        verify(clientRepository).existsByNit(1234567890L);
        verify(clientRepository).existsByEmail("juan@example.com");
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    @DisplayName("Test 2: Crear cliente con NIT duplicado debe lanzar BadRequestException")
    void testCreateClient_DuplicateNIT_ThrowsException() {
        // Given
        when(clientRepository.existsByNit(anyLong())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> clientService.createClient(requestDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ya existe un cliente con el NIT");

        verify(clientRepository).existsByNit(1234567890L);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test 3: Crear cliente con email duplicado debe lanzar BadRequestException")
    void testCreateClient_DuplicateEmail_ThrowsException() {
        // Given
        when(clientRepository.existsByNit(anyLong())).thenReturn(false);
        when(clientRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> clientService.createClient(requestDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ya existe un cliente con el email");

        verify(clientRepository).existsByNit(anyLong());
        verify(clientRepository).existsByEmail(anyString());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("Test 4: Obtener cliente por ID inexistente debe lanzar ResourceNotFoundException")
    void testGetClientById_NotFound_ThrowsException() {
        // Given
        Long clientId = 999L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> clientService.getClientById(clientId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cliente")
                .hasMessageContaining("id")
                .hasMessageContaining("999");

        verify(clientRepository).findById(clientId);
    }

    @Test
    @DisplayName("Test 5: Buscar cliente por NIT exitosamente")
    void testGetClientByNit_Success() {
        // Given
        when(clientRepository.findByNit(anyLong())).thenReturn(Optional.of(client));
        when(clientMapper.toResponseDTO(any(Client.class))).thenReturn(responseDTO);

        // When
        ClientResponseDTO result = clientService.getClientByNit(1234567890L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNit()).isEqualTo(1234567890L);
        verify(clientRepository).findByNit(1234567890L);
    }

    @Test
    @DisplayName("Test 6: Listar todos los clientes activos correctamente")
    void testGetAllActiveClients_ReturnsActiveClientsOnly() {
        // Given
        Client activeClient1 = Client.builder().id(1L).nombre("Juan").activo(true).build();
        Client activeClient2 = Client.builder().id(2L).nombre("María").activo(true).build();
        List<Client> activeClients = Arrays.asList(activeClient1, activeClient2);

        when(clientRepository.findAllActive()).thenReturn(activeClients);
        when(clientMapper.toResponseDTO(any(Client.class)))
                .thenReturn(responseDTO)
                .thenReturn(ClientResponseDTO.builder().id(2L).nombre("María").build());

        // When
        List<ClientResponseDTO> result = clientService.getAllActiveClients();

        // Then
        assertThat(result).hasSize(2);
        verify(clientRepository).findAllActive();
        verify(clientMapper, times(2)).toResponseDTO(any(Client.class));
    }

    @Test
    @DisplayName("Test 7: Actualizar cliente con validación de NIT y email únicos")
    void testUpdateClient_ValidatesUniqueConstraints() {
        // Given
        Long clientId = 1L;
        ClientRequestDTO updateDTO = ClientRequestDTO.builder()
                .nombre("Juan Carlos")
                .apellido("Pérez")
                .nit(9876543210L)
                .email("nuevo@example.com")
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.existsByNit(9876543210L)).thenReturn(false);
        when(clientRepository.existsByEmail("nuevo@example.com")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toResponseDTO(any(Client.class))).thenReturn(responseDTO);

        // When
        ClientResponseDTO result = clientService.updateClient(clientId, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(clientRepository).findById(clientId);
        verify(clientRepository).existsByNit(9876543210L);
        verify(clientRepository).existsByEmail("nuevo@example.com");
        verify(clientMapper).updateEntityFromDTO(updateDTO, client);
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("Test 8: Actualizar cliente con mismo NIT no debe validar duplicado")
    void testUpdateClient_SameNIT_NoValidation() {
        // Given
        Long clientId = 1L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(clientMapper.toResponseDTO(any(Client.class))).thenReturn(responseDTO);

        // When
        clientService.updateClient(clientId, requestDTO);

        // Then
        verify(clientRepository).findById(clientId);
        verify(clientRepository, never()).existsByNit(anyLong());
        verify(clientRepository).save(client);
    }

    @Test
    @DisplayName("Test 9: Búsqueda de clientes con múltiples resultados y mapeo correcto")
    void testSearchClients_MultipleResults() {
        // Given
        String searchQuery = "Pérez";
        Client client1 = Client.builder().id(1L).nombre("Juan").apellido("Pérez").build();
        Client client2 = Client.builder().id(2L).nombre("Pedro").apellido("Pérez").build();
        Client client3 = Client.builder().id(3L).nombre("Ana").apellido("Pérez García").build();

        when(clientRepository.searchClients(searchQuery))
                .thenReturn(Arrays.asList(client1, client2, client3));
        when(clientMapper.toResponseDTO(any(Client.class)))
                .thenReturn(responseDTO)
                .thenReturn(ClientResponseDTO.builder().id(2L).build())
                .thenReturn(ClientResponseDTO.builder().id(3L).build());

        // When
        List<ClientResponseDTO> result = clientService.searchClients(searchQuery);

        // Then
        assertThat(result).hasSize(3);
        verify(clientRepository).searchClients(searchQuery);
        verify(clientMapper, times(3)).toResponseDTO(any(Client.class));
    }

    @Test
    @DisplayName("Test 10: Eliminación lógica de cliente (soft delete) cambia estado a inactivo")
    void testDeleteClient_SoftDelete_SetsInactive() {
        // Given
        Long clientId = 1L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // When
        clientService.deleteClient(clientId);

        // Then
        verify(clientRepository).findById(clientId);
        verify(clientRepository).save(argThat(c -> !c.getActivo()));
        assertThat(client.getActivo()).isFalse();
    }
}

