package com.suitecrm.opportunity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "opportunities", schema = "opportunity_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Opportunity {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "opportunity_type", length = 50) private String opportunityType;
    @Column(name = "account_id") private UUID accountId;
    @Column(name = "account_name", length = 255) private String accountName;
    @Column(name = "campaign_id") private UUID campaignId;
    @Column(name = "campaign_name", length = 255) private String campaignName;
    @Column(name = "lead_source", length = 100) private String leadSource;
    @Column(name = "amount") private BigDecimal amount;
    @Column(name = "amount_usdollar") private BigDecimal amountUsdollar;
    @Column(name = "currency_id") private UUID currencyId;
    @Column(name = "date_closed") private LocalDate dateClosed;
    @Column(name = "next_step", length = 255) private String nextStep;
    @Column(name = "sales_stage", length = 50) private String salesStage;
    @Column(name = "probability") private BigDecimal probability;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
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
