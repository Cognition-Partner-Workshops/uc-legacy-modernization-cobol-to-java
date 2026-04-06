package com.suitecrm.template.repository;
import com.suitecrm.template.entity.EmailTemplate;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {
    Page<EmailTemplate> findByDeletedFalse(Pageable pageable);
    Optional<EmailTemplate> findByIdAndDeletedFalse(UUID id);
    List<EmailTemplate> findByTypeAndDeletedFalse(String type);
}
