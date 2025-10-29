package dev.galarza.factus.invoice.repository;

import dev.galarza.factus.invoice.entity.Invoice;
import dev.galarza.factus.invoice.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByNumeroFactura(String numeroFactura);

    List<Invoice> findByClientId(Long clientId);

    List<Invoice> findByEstado(InvoiceStatus estado);

    @Query("SELECT i FROM Invoice i WHERE i.fechaEmision BETWEEN :startDate AND :endDate")
    List<Invoice> findByDateRange(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT i FROM Invoice i WHERE i.client.id = :clientId AND i.estado = :estado")
    List<Invoice> findByClientAndEstado(@Param("clientId") Long clientId,
                                        @Param("estado") InvoiceStatus estado);

    @Query("SELECT i FROM Invoice i WHERE i.fechaEmision BETWEEN :startDate AND :endDate " +
           "AND i.estado = :estado")
    List<Invoice> findByDateRangeAndEstado(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("estado") InvoiceStatus estado);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.numeroFactura, LENGTH(i.serie) + 2) AS integer)), 0) " +
           "FROM Invoice i WHERE i.serie = :serie")
    Integer findLastNumberBySerie(@Param("serie") String serie);

    boolean existsByNumeroFactura(String numeroFactura);
}

