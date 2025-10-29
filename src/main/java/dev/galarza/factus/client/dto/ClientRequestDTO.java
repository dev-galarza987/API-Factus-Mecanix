package dev.galarza.factus.client.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    private String apellido;

    @NotNull(message = "El NIT es obligatorio")
    @Min(value = 1000000000L, message = "El NIT debe tener 10 dígitos")
    @Max(value = 9999999999L, message = "El NIT debe tener 10 dígitos")
    private Long nit;

    @Email(message = "Email debe ser válido")
    private String email;

    @Size(max = 20)
    private String telefono;

    @Size(max = 200)
    private String direccion;

    @Size(max = 100)
    private String ciudad;

    @Size(max = 100)
    private String departamento;
}

