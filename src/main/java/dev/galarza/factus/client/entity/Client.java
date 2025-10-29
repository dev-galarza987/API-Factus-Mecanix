package dev.galarza.factus.client.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100, name = "first_name")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100, name = "last_name")
    private String apellido;

    @NotNull(message = "El NIT es obligatorio")
    @Min(value = 1000000000L, message = "El NIT debe tener 10 dígitos")
    @Max(value = 9999999999L, message = "El NIT debe tener 10 dígitos")
    @Column(nullable = false, unique = true)
    private Long nit;

    @Email(message = "Email debe ser válido")
    @Column(unique = true)
    private String email;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Size(max = 200)
    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String departamento;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean activo = true;
}

