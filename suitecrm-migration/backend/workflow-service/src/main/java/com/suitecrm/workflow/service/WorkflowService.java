package com.suitecrm.workflow.service;

import com.suitecrm.workflow.dto.*;
import com.suitecrm.workflow.entity.*;
import com.suitecrm.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowActionRepository workflowActionRepository;
    private final WorkflowConditionRepository workflowConditionRepository;
    private final ProcessedWorkflowRepository processedWorkflowRepository;

    @Transactional(readOnly = true)
    public Page<WorkflowDto> listWorkflows(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return workflowRepository.findByDeletedFalse(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public WorkflowDto getWorkflow(UUID id) {
        Workflow wf = workflowRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with id: " + id));
        return toDto(wf);
    }

    public WorkflowDto createWorkflow(WorkflowCreateRequest request, UUID createdBy) {
        log.info("Creating workflow: name={}", request.getName());
        Workflow wf = Workflow.builder()
                .name(request.getName())
                .flowModule(request.getFlowModule())
                .status(request.getStatus() != null ? request.getStatus() : "Active")
                .runWhen(request.getRunWhen())
                .triggerType(request.getTriggerType())
                .afterDate(request.getAfterDate())
                .repeatRuns(request.getRepeatRuns())
                .multipleRuns(request.getMultipleRuns() != null ? request.getMultipleRuns() : false)
                .description(request.getDescription())
                .assignedUserId(request.getAssignedUserId())
                .createdBy(createdBy)
                .build();
        Workflow saved = workflowRepository.save(wf);
        log.info("Created workflow: id={}", saved.getId());
        return toDto(saved);
    }

    public WorkflowDto updateWorkflow(UUID id, WorkflowCreateRequest request) {
        Workflow existing = workflowRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with id: " + id));
        existing.setName(request.getName());
        existing.setFlowModule(request.getFlowModule());
        existing.setStatus(request.getStatus());
        existing.setRunWhen(request.getRunWhen());
        existing.setTriggerType(request.getTriggerType());
        existing.setAfterDate(request.getAfterDate());
        existing.setRepeatRuns(request.getRepeatRuns());
        existing.setMultipleRuns(request.getMultipleRuns());
        existing.setDescription(request.getDescription());
        existing.setAssignedUserId(request.getAssignedUserId());
        Workflow saved = workflowRepository.save(existing);
        return toDto(saved);
    }

    public void deleteWorkflow(UUID id) {
        Workflow wf = workflowRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with id: " + id));
        wf.setDeleted(true);
        workflowRepository.save(wf);
    }

    @Transactional(readOnly = true)
    public List<WorkflowAction> getWorkflowActions(UUID workflowId) {
        return workflowActionRepository.findByWorkflowIdAndDeletedFalseOrderByOrderNum(workflowId);
    }

    @Transactional(readOnly = true)
    public List<WorkflowCondition> getWorkflowConditions(UUID workflowId) {
        return workflowConditionRepository.findByWorkflowIdAndDeletedFalseOrderByOrderNum(workflowId);
    }

    @Transactional(readOnly = true)
    public Page<ProcessedWorkflow> getProcessedWorkflows(UUID workflowId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateProcessed").descending());
        return processedWorkflowRepository.findByWorkflowIdAndDeletedFalse(workflowId, pageable);
    }

    public void processWorkflow(UUID workflowId, UUID beanId, String beanModule) {
        if (!processedWorkflowRepository.existsByWorkflowIdAndBeanIdAndDeletedFalse(workflowId, beanId)) {
            ProcessedWorkflow pw = ProcessedWorkflow.builder()
                    .workflowId(workflowId)
                    .beanId(beanId)
                    .beanModule(beanModule)
                    .build();
            processedWorkflowRepository.save(pw);
        }
    }

    @Transactional(readOnly = true)
    public List<Workflow> getActiveWorkflowsByModule(String module) {
        return workflowRepository.findByFlowModuleAndStatusAndDeletedFalse(module, "Active");
    }

    private WorkflowDto toDto(Workflow entity) {
        return WorkflowDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .flowModule(entity.getFlowModule())
                .status(entity.getStatus())
                .runWhen(entity.getRunWhen())
                .triggerType(entity.getTriggerType())
                .afterDate(entity.getAfterDate())
                .repeatRuns(entity.getRepeatRuns())
                .multipleRuns(entity.getMultipleRuns())
                .description(entity.getDescription())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }
}
