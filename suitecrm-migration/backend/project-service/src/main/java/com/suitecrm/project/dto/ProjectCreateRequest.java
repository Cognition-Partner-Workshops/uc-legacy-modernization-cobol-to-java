package com.suitecrm.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 255, message = "Project name must not exceed 255 characters")
    private String name;

    private String status;
    private String priority;
    private LocalDate estimatedStartDate;
    private LocalDate estimatedEndDate;
    private BigDecimal estimatedCost;
    private String description;
    private Boolean overrideBusinessHours;
    private UUID assignedUserId;
    private UUID templateId;
}
