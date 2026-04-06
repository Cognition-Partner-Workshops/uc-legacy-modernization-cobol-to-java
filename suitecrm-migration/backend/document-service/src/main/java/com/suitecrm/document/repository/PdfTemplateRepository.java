package com.suitecrm.document.repository;

import com.suitecrm.document.entity.PdfTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PdfTemplateRepository extends JpaRepository<PdfTemplate, UUID> {
    Optional<PdfTemplate> findByIdAndDeletedFalse(UUID id);
    Page<PdfTemplate> findByDeletedFalse(Pageable pageable);
    List<PdfTemplate> findByModuleAndDeletedFalse(String module);
    Page<PdfTemplate> findByTypeAndDeletedFalse(String type, Pageable pageable);
    long countByDeletedFalse();
}
