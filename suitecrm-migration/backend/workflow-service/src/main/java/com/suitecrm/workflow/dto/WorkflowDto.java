package com.suitecrm.workflow.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDto {
    private UUID id;
    private String name;
    private String flowModule;
    private String status;
    private String runWhen;
    private String triggerType;
    private String afterDate;
    private Integer repeatRuns;
    private Boolean multipleRuns;
    private String description;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
