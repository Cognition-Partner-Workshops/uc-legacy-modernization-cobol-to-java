package com.suitecrm.workflow.repository;

import com.suitecrm.workflow.entity.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    Optional<Workflow> findByIdAndDeletedFalse(UUID id);
    Page<Workflow> findByDeletedFalse(Pageable pageable);
    Page<Workflow> findByStatusAndDeletedFalse(String status, Pageable pageable);
    Page<Workflow> findByFlowModuleAndDeletedFalse(String flowModule, Pageable pageable);
    List<Workflow> findByFlowModuleAndStatusAndDeletedFalse(String flowModule, String status);
}
