package com.suitecrm.template.repository;
import com.suitecrm.template.entity.PdfTemplate;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface PdfTemplateRepository extends JpaRepository<PdfTemplate, UUID> {
    Page<PdfTemplate> findByDeletedFalse(Pageable pageable);
    Optional<PdfTemplate> findByIdAndDeletedFalse(UUID id);
    List<PdfTemplate> findByModuleNameAndDeletedFalse(String moduleName);
    List<PdfTemplate> findByTypeAndDeletedFalse(String type);
}
