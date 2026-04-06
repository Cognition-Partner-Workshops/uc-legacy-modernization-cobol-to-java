package com.suitecrm.opportunity.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityDto {
    private UUID id;
    private String name;
    private UUID accountId;
    private String accountName;
    private String salesStage;
    private BigDecimal amount;
    private String currencyId;
    private Integer probability;
    private LocalDate dateClosed;
    private String nextStep;
    private String leadSource;
    private String opportunityType;
    private String description;
    private UUID campaignId;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
