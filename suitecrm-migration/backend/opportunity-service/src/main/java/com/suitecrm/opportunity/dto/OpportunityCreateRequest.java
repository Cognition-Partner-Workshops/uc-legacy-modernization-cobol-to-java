package com.suitecrm.opportunity.dto;

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
public class OpportunityCreateRequest {

    @NotBlank(message = "Opportunity name is required")
    @Size(max = 255)
    private String name;

    private UUID accountId;

    @Size(max = 50)
    private String salesStage;

    private BigDecimal amount;
    private String currencyId;
    private Integer probability;
    private LocalDate dateClosed;

    @Size(max = 255)
    private String nextStep;

    @Size(max = 100)
    private String leadSource;

    @Size(max = 100)
    private String opportunityType;

    private String description;
    private UUID campaignId;
    private UUID assignedUserId;
}
