package dev.galarza.factus.security.controller;

import dev.galarza.factus.security.dto.JwtResponseDTO;
import dev.galarza.factus.security.dto.LoginRequestDTO;
import dev.galarza.factus.security.dto.RegisterRequestDTO;
import dev.galarza.factus.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "API de autenticación y registro")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        JwtResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        String message = authService.register(registerRequest);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
}

