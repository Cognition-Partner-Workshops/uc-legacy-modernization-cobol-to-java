package com.suitecrm.campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "campaign_log", schema = "campaign_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "target_tracker_key", length = 36)
    private String targetTrackerKey;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "target_type", length = 100)
    private String targetType;

    @Column(name = "activity_type", length = 100)
    private String activityType;

    @Column(name = "activity_date")
    private LocalDateTime activityDate;

    @Column(name = "related_id")
    private UUID relatedId;

    @Column(name = "related_type", length = 100)
    private String relatedType;

    @Column(name = "archived")
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "hits")
    @Builder.Default
    private Integer hits = 0;

    @Column(name = "list_id")
    private UUID listId;

    @Column(name = "more_information", length = 255)
    private String moreInformation;

    @Column(name = "marketing_id")
    private UUID marketingId;

    @Column(name = "date_entered", nullable = false, updatable = false)
    private LocalDateTime dateEntered;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (activityDate == null) activityDate = LocalDateTime.now();
    }
}
