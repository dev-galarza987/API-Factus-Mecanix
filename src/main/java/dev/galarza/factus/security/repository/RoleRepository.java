package dev.galarza.factus.security.repository;

import dev.galarza.factus.security.entity.Role;
import dev.galarza.factus.security.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNombre(RoleName nombre);

    boolean existsByNombre(RoleName nombre);
}

