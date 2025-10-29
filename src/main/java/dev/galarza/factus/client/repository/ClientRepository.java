package dev.galarza.factus.client.repository;

import dev.galarza.factus.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByNit(Long nit);

    Optional<Client> findByEmail(String email);

    @Query("SELECT c FROM Client c WHERE c.activo = true")
    List<Client> findAllActive();

    @Query("SELECT c FROM Client c WHERE " +
           "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "CAST(c.nit AS string) LIKE CONCAT('%', :search, '%'))")
    List<Client> searchClients(@Param("search") String search);

    boolean existsByNit(Long nit);

    boolean existsByEmail(String email);
}

