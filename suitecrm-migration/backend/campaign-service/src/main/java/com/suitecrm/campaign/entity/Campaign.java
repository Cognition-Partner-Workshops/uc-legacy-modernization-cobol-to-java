package com.suitecrm.campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "campaigns", schema = "campaign_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Campaign {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "campaign_type", length = 50) private String campaignType;
    @Column(name = "status", length = 50) private String status;
    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;
    @Column(name = "budget") private BigDecimal budget;
    @Column(name = "actual_cost") private BigDecimal actualCost;
    @Column(name = "expected_cost") private BigDecimal expectedCost;
    @Column(name = "expected_revenue") private BigDecimal expectedRevenue;
    @Column(name = "impressions") private Integer impressions;
    @Column(name = "currency_id") private UUID currencyId;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "objective", columnDefinition = "TEXT") private String objective;
    @Column(name = "content", columnDefinition = "TEXT") private String content;
    @Column(name = "tracker_text", length = 255) private String trackerText;
    @Column(name = "tracker_key", length = 255) private String trackerKey;
    @Column(name = "tracker_count") @Builder.Default private Integer trackerCount = 0;
    @Column(name = "refer_url", length = 500) private String referUrl;
    @Column(name = "frequency", length = 50) private String frequency;
    @Column(name = "survey_id") private UUID surveyId;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "modified_user_id") private UUID modifiedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
    @Version @Column(name = "version") private Long version;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
