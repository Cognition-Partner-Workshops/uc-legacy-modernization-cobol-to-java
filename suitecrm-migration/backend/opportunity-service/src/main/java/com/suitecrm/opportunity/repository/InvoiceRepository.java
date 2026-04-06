package com.suitecrm.opportunity.repository;

import com.suitecrm.opportunity.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByIdAndDeletedFalse(UUID id);
    Page<Invoice> findByDeletedFalse(Pageable pageable);
    Page<Invoice> findByAccountIdAndDeletedFalse(UUID accountId, Pageable pageable);
    Page<Invoice> findByOpportunityIdAndDeletedFalse(UUID opportunityId, Pageable pageable);
    Page<Invoice> findByStatusAndDeletedFalse(String status, Pageable pageable);
    Page<Invoice> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);
    long countByDeletedFalse();
}
