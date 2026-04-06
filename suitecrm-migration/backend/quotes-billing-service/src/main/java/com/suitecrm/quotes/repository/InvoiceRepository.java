package com.suitecrm.quotes.repository;

import com.suitecrm.quotes.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findByDeletedFalse(Pageable pageable);
    Optional<Invoice> findByIdAndDeletedFalse(UUID id);
    List<Invoice> findByQuoteIdAndDeletedFalse(UUID quoteId);
    List<Invoice> findByStatusAndDeletedFalse(String status);
    List<Invoice> findByAccountIdAndDeletedFalse(UUID accountId);
}
