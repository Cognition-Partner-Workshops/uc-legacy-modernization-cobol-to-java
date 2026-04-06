package com.suitecrm.contact.repository;

import com.suitecrm.contact.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Optional<Lead> findByIdAndDeletedFalse(UUID id);

    Page<Lead> findByDeletedFalse(Pageable pageable);

    Page<Lead> findByStatusAndDeletedFalse(String status, Pageable pageable);

    Page<Lead> findByAssignedUserIdAndDeletedFalse(UUID assignedUserId, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE l.deleted = false AND " +
            "(LOWER(l.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.company) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(l.emailPrimary) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Lead> searchLeads(@Param("search") String search, Pageable pageable);

    long countByStatusAndDeletedFalse(String status);

    Page<Lead> findByConvertedFalseAndDeletedFalse(Pageable pageable);
}
