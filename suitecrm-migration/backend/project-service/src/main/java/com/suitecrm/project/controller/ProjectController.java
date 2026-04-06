package com.suitecrm.project.controller;

import com.suitecrm.project.dto.*;
import com.suitecrm.project.entity.ProjectResource;
import com.suitecrm.project.entity.ProjectTemplate;
import com.suitecrm.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Page<ProjectDto>> listProjects(Pageable pageable) {
        return ResponseEntity.ok(projectService.listProjects(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ProjectDto> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable UUID id,
                                                     @Valid @RequestBody ProjectCreateRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Page<ProjectDto>> searchProjects(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(projectService.searchProjects(query, pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Page<ProjectDto>> getProjectsByStatus(@PathVariable String status, Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectsByStatus(status, pageable));
    }

    // ==================== Project Tasks ====================

    @GetMapping("/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<ProjectTaskDto>> getProjectTasks(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectTasks(projectId));
    }

    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ProjectTaskDto> getProjectTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(projectService.getProjectTask(taskId));
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectTaskDto> createProjectTask(@Valid @RequestBody ProjectTaskCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProjectTask(request));
    }

    @PutMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectTaskDto> updateProjectTask(@PathVariable UUID taskId,
                                                             @Valid @RequestBody ProjectTaskCreateRequest request) {
        return ResponseEntity.ok(projectService.updateProjectTask(taskId, request));
    }

    @DeleteMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteProjectTask(@PathVariable UUID taskId) {
        projectService.deleteProjectTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/milestones")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<ProjectTaskDto>> getMilestones(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getMilestones(projectId));
    }

    @GetMapping("/tasks/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProjectTaskDto>> getOverdueTasks() {
        return ResponseEntity.ok(projectService.getOverdueTasks());
    }

    // ==================== Templates ====================

    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<ProjectTemplate>> listTemplates(Pageable pageable) {
        return ResponseEntity.ok(projectService.listTemplates(pageable));
    }

    @GetMapping("/templates/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProjectTemplate>> getActiveTemplates() {
        return ResponseEntity.ok(projectService.getActiveTemplates());
    }

    @PostMapping("/templates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectTemplate> createTemplate(@RequestBody ProjectTemplate template) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createTemplate(template));
    }

    // ==================== Resources ====================

    @GetMapping("/{projectId}/resources")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<ProjectResource>> getProjectResources(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectResources(projectId));
    }

    @PostMapping("/{projectId}/resources")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectResource> addResource(@PathVariable UUID projectId,
                                                        @RequestBody ProjectResource resource) {
        resource.setProjectId(projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.addResource(resource));
    }
}
