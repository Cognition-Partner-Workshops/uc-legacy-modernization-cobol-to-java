package com.suitecrm.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCreateRequest {

    @NotBlank(message = "Workflow name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 100)
    private String flowModule;

    @Size(max = 50)
    private String status;

    @Size(max = 50)
    private String runWhen;

    @Size(max = 50)
    private String triggerType;

    private String afterDate;
    private Integer repeatRuns;
    private Boolean multipleRuns;
    private String description;
    private UUID assignedUserId;
}
