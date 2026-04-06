package com.suitecrm.project.repository;

import com.suitecrm.project.entity.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, UUID> {
    List<TaskTemplate> findByProjectTemplateIdAndDeletedFalseOrderByOrderNumberAsc(UUID projectTemplateId);
}
