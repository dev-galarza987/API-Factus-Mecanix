package dev.galarza.factus.security.service;

import dev.galarza.factus.exception.BadRequestException;
import dev.galarza.factus.security.dto.JwtResponseDTO;
import dev.galarza.factus.security.dto.LoginRequestDTO;
import dev.galarza.factus.security.dto.RegisterRequestDTO;
import dev.galarza.factus.security.entity.Role;
import dev.galarza.factus.security.entity.RoleName;
import dev.galarza.factus.security.entity.User;
import dev.galarza.factus.security.jwt.JwtTokenProvider;
import dev.galarza.factus.security.repository.RoleRepository;
import dev.galarza.factus.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public JwtResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        return JwtResponseDTO.builder()
                .token(jwt)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public String register(RegisterRequestDTO registerRequest) {
        // Validar username único
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("El username ya está en uso");
        }

        // Validar email único
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("El email ya está en uso");
        }

        // Crear nuevo usuario
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .nombre(registerRequest.getNombre())
                .apellido(registerRequest.getApellido())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        // Asignar roles
        Set<Role> roles = new HashSet<>();
        if (registerRequest.getRoles() == null || registerRequest.getRoles().isEmpty()) {
            // Rol por defecto: USER
            Role userRole = roleRepository.findByNombre(RoleName.ROLE_USER)
                    .orElseThrow(() -> new BadRequestException("Rol USER no encontrado"));
            roles.add(userRole);
        } else {
            registerRequest.getRoles().forEach(roleName -> {
                try {
                    RoleName roleEnum = RoleName.valueOf(roleName);
                    Role role = roleRepository.findByNombre(roleEnum)
                            .orElseThrow(() -> new BadRequestException("Rol no encontrado: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Rol inválido: " + roleName);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return "Usuario registrado exitosamente";
    }
}

