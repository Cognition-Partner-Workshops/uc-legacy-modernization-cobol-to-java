package com.suitecrm.project.service;

import com.suitecrm.project.dto.*;
import com.suitecrm.project.entity.Project;
import com.suitecrm.project.entity.ProjectResource;
import com.suitecrm.project.entity.ProjectTask;
import com.suitecrm.project.entity.ProjectTemplate;
import com.suitecrm.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectTemplateRepository projectTemplateRepository;
    private final ProjectResourceRepository projectResourceRepository;

    // ==================== Project CRUD ====================

    @Transactional(readOnly = true)
    public Page<ProjectDto> listProjects(Pageable pageable) {
        return projectRepository.findByDeletedFalse(pageable).map(this::toProjectDto);
    }

    @Transactional(readOnly = true)
    public ProjectDto getProject(UUID id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));
        return toProjectDto(project);
    }

    public ProjectDto createProject(ProjectCreateRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .status(request.getStatus() != null ? request.getStatus() : "Draft")
                .priority(request.getPriority() != null ? request.getPriority() : "Medium")
                .estimatedStartDate(request.getEstimatedStartDate())
                .estimatedEndDate(request.getEstimatedEndDate())
                .estimatedCost(request.getEstimatedCost())
                .description(request.getDescription())
                .overrideBusinessHours(request.getOverrideBusinessHours())
                .assignedUserId(request.getAssignedUserId())
                .build();

        Project saved = projectRepository.save(project);
        log.info("Created project: {} ({})", saved.getName(), saved.getId());

        if (request.getTemplateId() != null) {
            applyTemplate(saved.getId(), request.getTemplateId());
        }

        return toProjectDto(saved);
    }

    public ProjectDto updateProject(UUID id, ProjectCreateRequest request) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));

        if (request.getName() != null) project.setName(request.getName());
        if (request.getStatus() != null) project.setStatus(request.getStatus());
        if (request.getPriority() != null) project.setPriority(request.getPriority());
        if (request.getEstimatedStartDate() != null) project.setEstimatedStartDate(request.getEstimatedStartDate());
        if (request.getEstimatedEndDate() != null) project.setEstimatedEndDate(request.getEstimatedEndDate());
        if (request.getEstimatedCost() != null) project.setEstimatedCost(request.getEstimatedCost());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getOverrideBusinessHours() != null) project.setOverrideBusinessHours(request.getOverrideBusinessHours());
        if (request.getAssignedUserId() != null) project.setAssignedUserId(request.getAssignedUserId());

        Project saved = projectRepository.save(project);
        log.info("Updated project: {} ({})", saved.getName(), saved.getId());
        return toProjectDto(saved);
    }

    public void deleteProject(UUID id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Project not found: " + id));
        project.setDeleted(true);
        projectRepository.save(project);
        log.info("Soft-deleted project: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> searchProjects(String query, Pageable pageable) {
        return projectRepository.searchByName(query, pageable).map(this::toProjectDto);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjectsByStatus(String status, Pageable pageable) {
        return projectRepository.findByStatus(status, pageable).map(this::toProjectDto);
    }

    // ==================== Project Tasks ====================

    @Transactional(readOnly = true)
    public List<ProjectTaskDto> getProjectTasks(UUID projectId) {
        return projectTaskRepository.findByProjectIdAndDeletedFalseOrderByOrderNumberAsc(projectId)
                .stream().map(this::toProjectTaskDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectTaskDto getProjectTask(UUID taskId) {
        ProjectTask task = projectTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("Project task not found: " + taskId));
        return toProjectTaskDto(task);
    }

    public ProjectTaskDto createProjectTask(ProjectTaskCreateRequest request) {
        projectRepository.findByIdAndDeletedFalse(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found: " + request.getProjectId()));

        ProjectTask task = ProjectTask.builder()
                .projectId(request.getProjectId())
                .name(request.getName())
                .status(request.getStatus() != null ? request.getStatus() : "Not Started")
                .priority(request.getPriority() != null ? request.getPriority() : "Medium")
                .orderNumber(request.getOrderNumber())
                .estimatedEffort(request.getEstimatedEffort())
                .dateStart(request.getDateStart())
                .dateFinish(request.getDateFinish())
                .dateDue(request.getDateDue())
                .duration(request.getDuration())
                .durationUnit(request.getDurationUnit() != null ? request.getDurationUnit() : "Days")
                .parentTaskId(request.getParentTaskId())
                .milestoneFlag(request.getMilestoneFlag() != null ? request.getMilestoneFlag() : false)
                .predecessors(request.getPredecessors())
                .description(request.getDescription())
                .assignedUserId(request.getAssignedUserId())
                .build();

        ProjectTask saved = projectTaskRepository.save(task);
        log.info("Created project task: {} for project {}", saved.getName(), saved.getProjectId());
        return toProjectTaskDto(saved);
    }

    public ProjectTaskDto updateProjectTask(UUID taskId, ProjectTaskCreateRequest request) {
        ProjectTask task = projectTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("Project task not found: " + taskId));

        if (request.getName() != null) task.setName(request.getName());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getOrderNumber() != null) task.setOrderNumber(request.getOrderNumber());
        if (request.getEstimatedEffort() != null) task.setEstimatedEffort(request.getEstimatedEffort());
        if (request.getDateStart() != null) task.setDateStart(request.getDateStart());
        if (request.getDateFinish() != null) task.setDateFinish(request.getDateFinish());
        if (request.getDateDue() != null) task.setDateDue(request.getDateDue());
        if (request.getDuration() != null) task.setDuration(request.getDuration());
        if (request.getDurationUnit() != null) task.setDurationUnit(request.getDurationUnit());
        if (request.getParentTaskId() != null) task.setParentTaskId(request.getParentTaskId());
        if (request.getMilestoneFlag() != null) task.setMilestoneFlag(request.getMilestoneFlag());
        if (request.getPredecessors() != null) task.setPredecessors(request.getPredecessors());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getAssignedUserId() != null) task.setAssignedUserId(request.getAssignedUserId());

        ProjectTask saved = projectTaskRepository.save(task);
        log.info("Updated project task: {}", saved.getId());
        return toProjectTaskDto(saved);
    }

    public void deleteProjectTask(UUID taskId) {
        ProjectTask task = projectTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("Project task not found: " + taskId));
        task.setDeleted(true);
        projectTaskRepository.save(task);
        log.info("Soft-deleted project task: {}", taskId);
    }

    @Transactional(readOnly = true)
    public List<ProjectTaskDto> getMilestones(UUID projectId) {
        return projectTaskRepository.findMilestonesByProjectId(projectId)
                .stream().map(this::toProjectTaskDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectTaskDto> getOverdueTasks() {
        return projectTaskRepository.findOverdueTasks(LocalDate.now())
                .stream().map(this::toProjectTaskDto).collect(Collectors.toList());
    }

    // ==================== Project Templates ====================

    @Transactional(readOnly = true)
    public Page<ProjectTemplate> listTemplates(Pageable pageable) {
        return projectTemplateRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public List<ProjectTemplate> getActiveTemplates() {
        return projectTemplateRepository.findActiveTemplates();
    }

    public ProjectTemplate createTemplate(ProjectTemplate template) {
        return projectTemplateRepository.save(template);
    }

    public void applyTemplate(UUID projectId, UUID templateId) {
        projectTemplateRepository.findByIdAndDeletedFalse(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));
        log.info("Applied template {} to project {}", templateId, projectId);
    }

    // ==================== Project Resources ====================

    @Transactional(readOnly = true)
    public List<ProjectResource> getProjectResources(UUID projectId) {
        return projectResourceRepository.findByProjectIdAndDeletedFalse(projectId);
    }

    public ProjectResource addResource(ProjectResource resource) {
        return projectResourceRepository.save(resource);
    }

    // ==================== Mappers ====================

    private ProjectDto toProjectDto(Project project) {
        Long taskCount = projectTaskRepository.countByProjectId(project.getId());
        Double completion = projectTaskRepository.getAverageCompletionByProjectId(project.getId());

        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .status(project.getStatus())
                .priority(project.getPriority())
                .estimatedStartDate(project.getEstimatedStartDate())
                .estimatedEndDate(project.getEstimatedEndDate())
                .actualStartDate(project.getActualStartDate())
                .actualEndDate(project.getActualEndDate())
                .estimatedCost(project.getEstimatedCost())
                .actualCost(project.getActualCost())
                .description(project.getDescription())
                .overrideBusinessHours(project.getOverrideBusinessHours())
                .assignedUserId(project.getAssignedUserId())
                .createdBy(project.getCreatedBy())
                .dateEntered(project.getDateEntered())
                .dateModified(project.getDateModified())
                .taskCount(taskCount)
                .completionPercentage(completion)
                .build();
    }

    private ProjectTaskDto toProjectTaskDto(ProjectTask task) {
        return ProjectTaskDto.builder()
                .id(task.getId())
                .projectId(task.getProjectId())
                .name(task.getName())
                .status(task.getStatus())
                .priority(task.getPriority())
                .percentComplete(task.getPercentComplete())
                .taskNumber(task.getTaskNumber())
                .orderNumber(task.getOrderNumber())
                .estimatedEffort(task.getEstimatedEffort())
                .actualEffort(task.getActualEffort())
                .utilization(task.getUtilization())
                .dateStart(task.getDateStart())
                .dateFinish(task.getDateFinish())
                .dateDue(task.getDateDue())
                .duration(task.getDuration())
                .durationUnit(task.getDurationUnit())
                .parentTaskId(task.getParentTaskId())
                .milestoneFlag(task.getMilestoneFlag())
                .predecessors(task.getPredecessors())
                .description(task.getDescription())
                .assignedUserId(task.getAssignedUserId())
                .createdBy(task.getCreatedBy())
                .dateEntered(task.getDateEntered())
                .dateModified(task.getDateModified())
                .build();
    }
}
