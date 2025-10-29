package dev.galarza.factus.security.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtResponseDTO {

    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
}

