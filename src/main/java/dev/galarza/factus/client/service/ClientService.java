package dev.galarza.factus.client.service;

import dev.galarza.factus.client.dto.ClientRequestDTO;
import dev.galarza.factus.client.dto.ClientResponseDTO;
import dev.galarza.factus.client.entity.Client;
import dev.galarza.factus.client.mapper.ClientMapper;
import dev.galarza.factus.client.repository.ClientRepository;
import dev.galarza.factus.exception.BadRequestException;
import dev.galarza.factus.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional
    public ClientResponseDTO createClient(ClientRequestDTO requestDTO) {
        // Validar NIT único
        if (clientRepository.existsByNit(requestDTO.getNit())) {
            throw new BadRequestException("Ya existe un cliente con el NIT: " + requestDTO.getNit());
        }

        // Validar email único si se proporciona
        if (requestDTO.getEmail() != null && clientRepository.existsByEmail(requestDTO.getEmail())) {
            throw new BadRequestException("Ya existe un cliente con el email: " + requestDTO.getEmail());
        }

        Client client = clientMapper.toEntity(requestDTO);
        client.setActivo(true);
        Client savedClient = clientRepository.save(client);

        return clientMapper.toResponseDTO(savedClient);
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        return clientMapper.toResponseDTO(client);
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getClientByNit(Long nit) {
        Client client = clientRepository.findByNit(nit)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "NIT", nit));
        return clientMapper.toResponseDTO(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAllActiveClients() {
        return clientRepository.findAllActive().stream()
                .map(clientMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> searchClients(String search) {
        return clientRepository.searchClients(search).stream()
                .map(clientMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClientResponseDTO updateClient(Long id, ClientRequestDTO requestDTO) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        // Validar NIT único si cambió
        if (!client.getNit().equals(requestDTO.getNit()) &&
            clientRepository.existsByNit(requestDTO.getNit())) {
            throw new BadRequestException("Ya existe un cliente con el NIT: " + requestDTO.getNit());
        }

        // Validar email único si cambió
        if (requestDTO.getEmail() != null &&
            !requestDTO.getEmail().equals(client.getEmail()) &&
            clientRepository.existsByEmail(requestDTO.getEmail())) {
            throw new BadRequestException("Ya existe un cliente con el email: " + requestDTO.getEmail());
        }

        clientMapper.updateEntityFromDTO(requestDTO, client);
        Client updatedClient = clientRepository.save(client);

        return clientMapper.toResponseDTO(updatedClient);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        // Soft delete
        client.setActivo(false);
        clientRepository.save(client);
    }

    @Transactional
    public void hardDeleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", "id", id);
        }
        clientRepository.deleteById(id);
    }
}

