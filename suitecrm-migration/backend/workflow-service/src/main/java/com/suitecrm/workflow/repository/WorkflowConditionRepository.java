package com.suitecrm.workflow.repository;

import com.suitecrm.workflow.entity.WorkflowCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowConditionRepository extends JpaRepository<WorkflowCondition, UUID> {
    List<WorkflowCondition> findByWorkflowIdAndDeletedFalseOrderByConditionOrderAsc(UUID workflowId);
}
