package dev.galarza.factus.config;

import dev.galarza.factus.security.entity.Role;
import dev.galarza.factus.security.entity.RoleName;
import dev.galarza.factus.security.entity.User;
import dev.galarza.factus.security.repository.RoleRepository;
import dev.galarza.factus.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Crear roles si no existen
        createRoleIfNotExists(RoleName.ROLE_ADMIN, "Administrador del sistema");
        createRoleIfNotExists(RoleName.ROLE_USER, "Usuario estándar");
        createRoleIfNotExists(RoleName.ROLE_VIEWER, "Usuario solo lectura");

        // Crear usuario admin si no existe
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByNombre(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@factus.com")
                    .nombre("Admin")
                    .apellido("Sistema")
                    .roles(roles)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            userRepository.save(admin);
            log.info("Usuario admin creado exitosamente");
            log.info("Username: admin");
            log.info("Password: admin123");
        }

        log.info("Inicialización de datos completada");
    }

    private void createRoleIfNotExists(RoleName roleName, String descripcion) {
        if (!roleRepository.existsByNombre(roleName)) {
            Role role = Role.builder()
                    .nombre(roleName)
                    .descripcion(descripcion)
                    .build();
            roleRepository.save(role);
            log.info("Rol {} creado", roleName);
        }
    }
}

