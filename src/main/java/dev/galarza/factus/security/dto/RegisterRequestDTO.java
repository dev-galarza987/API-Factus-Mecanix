package dev.galarza.factus.security.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "El password es obligatorio")
    @Size(min = 6, message = "El password debe tener al menos 6 caracteres")
    private String password;

    @Email(message = "Email debe ser válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @Size(max = 100)
    private String nombre;

    @Size(max = 100)
    private String apellido;

    private Set<String> roles;
}

