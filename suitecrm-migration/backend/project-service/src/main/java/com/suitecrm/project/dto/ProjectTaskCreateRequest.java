package com.suitecrm.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskCreateRequest {

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotBlank(message = "Task name is required")
    @Size(max = 255, message = "Task name must not exceed 255 characters")
    private String name;

    private String status;
    private String priority;
    private Integer orderNumber;
    private Integer estimatedEffort;
    private LocalDate dateStart;
    private LocalDate dateFinish;
    private LocalDate dateDue;
    private Integer duration;
    private String durationUnit;
    private UUID parentTaskId;
    private Boolean milestoneFlag;
    private String predecessors;
    private String description;
    private UUID assignedUserId;
}
