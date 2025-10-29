package dev.galarza.factus.client.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private Long nit;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String departamento;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

