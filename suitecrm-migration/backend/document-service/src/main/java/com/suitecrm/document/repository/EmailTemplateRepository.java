package com.suitecrm.document.repository;

import com.suitecrm.document.entity.EmailTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {
    Optional<EmailTemplate> findByIdAndDeletedFalse(UUID id);
    Page<EmailTemplate> findByDeletedFalse(Pageable pageable);
    Page<EmailTemplate> findByTypeAndDeletedFalse(String type, Pageable pageable);
    Page<EmailTemplate> findByPublishedTrueAndDeletedFalse(Pageable pageable);

    @Query("SELECT et FROM EmailTemplate et WHERE et.deleted = false AND " +
            "(LOWER(et.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(et.subject) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<EmailTemplate> searchTemplates(@Param("search") String search, Pageable pageable);

    long countByDeletedFalse();
}
