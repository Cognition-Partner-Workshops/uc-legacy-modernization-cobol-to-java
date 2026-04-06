package com.suitecrm.campaign.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDto {
    private UUID id;
    private String name;
    private String campaignType;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private BigDecimal actualCost;
    private BigDecimal expectedRevenue;
    private Integer expectedCost;
    private Integer impressions;
    private String description;
    private String objective;
    private String content;
    private String trackerText;
    private Integer trackerCount;
    private Integer referUrl;
    private String frequency;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
