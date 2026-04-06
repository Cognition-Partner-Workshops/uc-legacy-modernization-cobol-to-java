package com.suitecrm.quotes.repository;

import com.suitecrm.quotes.entity.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, UUID> {
    Page<Quote> findByDeletedFalse(Pageable pageable);
    Optional<Quote> findByIdAndDeletedFalse(UUID id);
    List<Quote> findByAccountIdAndDeletedFalse(UUID accountId);
    List<Quote> findByQuoteStageAndDeletedFalse(String stage);
    List<Quote> findByOpportunityIdAndDeletedFalse(UUID opportunityId);
}
