package com.suitecrm.cases.repository;

import com.suitecrm.cases.entity.SupportCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupportCaseRepository extends JpaRepository<SupportCase, UUID> {

    Optional<SupportCase> findByIdAndDeletedFalse(UUID id);
    Optional<SupportCase> findByCaseNumberAndDeletedFalse(Long caseNumber);
    Page<SupportCase> findByDeletedFalse(Pageable pageable);
    Page<SupportCase> findByStatusAndDeletedFalse(String status, Pageable pageable);
    Page<SupportCase> findByPriorityAndDeletedFalse(String priority, Pageable pageable);
    Page<SupportCase> findByAccountIdAndDeletedFalse(UUID accountId, Pageable pageable);
    Page<SupportCase> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);

    @Query("SELECT c FROM SupportCase c WHERE c.deleted = false AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "CAST(c.caseNumber AS string) LIKE CONCAT('%', :search, '%'))")
    Page<SupportCase> searchCases(@Param("search") String search, Pageable pageable);
}
