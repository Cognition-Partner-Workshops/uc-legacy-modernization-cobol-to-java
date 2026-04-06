package com.suitecrm.project.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    private UUID id;
    private String name;
    private String status;
    private String priority;
    private LocalDate estimatedStartDate;
    private LocalDate estimatedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String description;
    private Boolean overrideBusinessHours;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
    private Long taskCount;
    private Double completionPercentage;
}
