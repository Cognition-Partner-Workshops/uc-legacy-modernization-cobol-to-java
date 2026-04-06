package com.suitecrm.workflow.repository;

import com.suitecrm.workflow.entity.ProcessedWorkflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProcessedWorkflowRepository extends JpaRepository<ProcessedWorkflow, UUID> {
    Page<ProcessedWorkflow> findByWorkflowIdAndDeletedFalse(UUID workflowId, Pageable pageable);
    List<ProcessedWorkflow> findByBeanIdAndBeanModuleAndDeletedFalse(UUID beanId, String beanModule);
    long countByWorkflowIdAndDeletedFalse(UUID workflowId);
    boolean existsByWorkflowIdAndBeanIdAndDeletedFalse(UUID workflowId, UUID beanId);
}
