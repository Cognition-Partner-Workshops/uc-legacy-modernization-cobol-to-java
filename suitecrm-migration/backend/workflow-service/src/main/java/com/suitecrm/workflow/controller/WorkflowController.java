package com.suitecrm.workflow.controller;

import com.suitecrm.workflow.entity.Workflow;
import com.suitecrm.workflow.repository.WorkflowRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowRepository workflowRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Workflow>> listWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Workflow> workflows;
        if (status != null && !status.isBlank()) {
            workflows = workflowRepository.findByStatusAndDeletedFalse(status, pageable);
        } else {
            workflows = workflowRepository.findByDeletedFalse(pageable);
        }
        return ResponseEntity.ok(workflows);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Workflow> getWorkflow(@PathVariable UUID id) {
        return ResponseEntity.ok(workflowRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Workflow> createWorkflow(@Valid @RequestBody Workflow workflow) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflowRepository.save(workflow));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Workflow> updateWorkflow(@PathVariable UUID id, @Valid @RequestBody Workflow update) {
        Workflow existing = workflowRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));
        existing.setName(update.getName());
        existing.setFlowModule(update.getFlowModule());
        existing.setStatus(update.getStatus());
        existing.setRunWhen(update.getRunWhen());
        existing.setRunOn(update.getRunOn());
        existing.setRepeatRuns(update.getRepeatRuns());
        existing.setDescription(update.getDescription());
        return ResponseEntity.ok(workflowRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable UUID id) {
        Workflow workflow = workflowRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));
        workflow.setDeleted(true);
        workflowRepository.save(workflow);
        return ResponseEntity.noContent().build();
    }
}
