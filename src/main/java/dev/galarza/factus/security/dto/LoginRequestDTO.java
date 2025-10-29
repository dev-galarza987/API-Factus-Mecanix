package dev.galarza.factus.security.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "El password es obligatorio")
    private String password;
}

