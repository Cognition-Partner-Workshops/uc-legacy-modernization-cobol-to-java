package com.suitecrm.opportunity.repository;

import com.suitecrm.opportunity.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {

    Optional<Opportunity> findByIdAndDeletedFalse(UUID id);
    Page<Opportunity> findByDeletedFalse(Pageable pageable);
    Page<Opportunity> findBySalesStageAndDeletedFalse(String salesStage, Pageable pageable);
    Page<Opportunity> findByAccountIdAndDeletedFalse(UUID accountId, Pageable pageable);

    @Query("SELECT o FROM Opportunity o WHERE o.deleted = false AND " +
            "(LOWER(o.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.salesStage) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Opportunity> searchOpportunities(@Param("search") String search, Pageable pageable);
}
