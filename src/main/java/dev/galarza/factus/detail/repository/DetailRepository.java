package dev.galarza.factus.detail.repository;

import dev.galarza.factus.detail.entity.Detail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailRepository extends JpaRepository<Detail, Long> {

    List<Detail> findByInvoiceId(Long invoiceId);

    @Query("SELECT d FROM Detail d WHERE d.invoice.id = :invoiceId")
    List<Detail> findDetailsByInvoice(@Param("invoiceId") Long invoiceId);

    void deleteByInvoiceId(Long invoiceId);
}

