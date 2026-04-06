package com.suitecrm.campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "campaign_trackers", schema = "campaign_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignTracker {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tracker_name", nullable = false, length = 255)
    private String trackerName;

    @Column(name = "tracker_url", length = 500)
    private String trackerUrl;

    @Column(name = "tracker_key", length = 36)
    private String trackerKey;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "is_optout")
    @Builder.Default
    private Boolean isOptout = false;

    @Column(name = "date_entered", nullable = false, updatable = false)
    private LocalDateTime dateEntered;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        dateModified = LocalDateTime.now();
        if (trackerKey == null) trackerKey = UUID.randomUUID().toString();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
