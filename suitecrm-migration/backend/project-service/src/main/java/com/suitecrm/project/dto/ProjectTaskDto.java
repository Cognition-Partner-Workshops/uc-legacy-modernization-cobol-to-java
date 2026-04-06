package com.suitecrm.project.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskDto {

    private UUID id;
    private UUID projectId;
    private String projectName;
    private String name;
    private String status;
    private String priority;
    private Integer percentComplete;
    private Integer taskNumber;
    private Integer orderNumber;
    private Integer estimatedEffort;
    private Integer actualEffort;
    private Integer utilization;
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
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
