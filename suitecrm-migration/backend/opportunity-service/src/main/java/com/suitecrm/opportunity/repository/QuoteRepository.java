package com.suitecrm.opportunity.repository;

import com.suitecrm.opportunity.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    Optional<Quote> findByIdAndDeletedFalse(UUID id);
    Page<Quote> findByDeletedFalse(Pageable pageable);
    Page<Quote> findByAccountIdAndDeletedFalse(UUID accountId, Pageable pageable);
    Page<Quote> findByOpportunityIdAndDeletedFalse(UUID opportunityId, Pageable pageable);
    Page<Quote> findByQuoteStageAndDeletedFalse(String quoteStage, Pageable pageable);
    Page<Quote> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);
    long countByDeletedFalse();
}
