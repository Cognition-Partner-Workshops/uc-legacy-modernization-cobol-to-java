package com.suitecrm.workflow.repository;

import com.suitecrm.workflow.entity.WorkflowAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowActionRepository extends JpaRepository<WorkflowAction, UUID> {
    List<WorkflowAction> findByWorkflowIdAndDeletedFalseOrderByActionOrderAsc(UUID workflowId);
}
