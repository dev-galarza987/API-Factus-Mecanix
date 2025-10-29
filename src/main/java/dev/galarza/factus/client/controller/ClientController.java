package dev.galarza.factus.client.controller;

import dev.galarza.factus.client.dto.ClientRequestDTO;
import dev.galarza.factus.client.dto.ClientResponseDTO;
import dev.galarza.factus.client.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "API de gestión de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @Operation(summary = "Crear nuevo cliente")
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO requestDTO) {
        ClientResponseDTO response = clientService.createClient(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id) {
        ClientResponseDTO response = clientService.getClientById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nit/{nit}")
    @Operation(summary = "Obtener cliente por NIT")
    public ResponseEntity<ClientResponseDTO> getClientByNit(@PathVariable Long nit) {
        ClientResponseDTO response = clientService.getClientByNit(nit);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {
        List<ClientResponseDTO> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/active")
    @Operation(summary = "Listar clientes activos")
    public ResponseEntity<List<ClientResponseDTO>> getActiveClients() {
        List<ClientResponseDTO> clients = clientService.getAllActiveClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar clientes")
    public ResponseEntity<List<ClientResponseDTO>> searchClients(@RequestParam String query) {
        List<ClientResponseDTO> clients = clientService.searchClients(query);
        return ResponseEntity.ok(clients);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDTO requestDTO) {
        ClientResponseDTO response = clientService.updateClient(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar cliente (soft delete)")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @Operation(summary = "Eliminar cliente permanentemente")
    public ResponseEntity<Void> hardDeleteClient(@PathVariable Long id) {
        clientService.hardDeleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
