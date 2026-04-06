package com.suitecrm.campaign.dto;

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
public class CampaignCreateRequest {

    @NotBlank(message = "Campaign name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String campaignType;

    @Size(max = 50)
    private String status;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private BigDecimal actualCost;
    private BigDecimal expectedRevenue;
    private String description;
    private String objective;
    private String content;
    private UUID assignedUserId;
}
