package com.suitecrm.campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_marketing", schema = "campaign_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMarketing {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "from_name", length = 255)
    private String fromName;

    @Column(name = "from_addr", length = 255)
    private String fromAddr;

    @Column(name = "reply_to_name", length = 255)
    private String replyToName;

    @Column(name = "reply_to_addr", length = 255)
    private String replyToAddr;

    @Column(name = "inbound_email_id")
    private UUID inboundEmailId;

    @Column(name = "date_start")
    private LocalDateTime dateStart;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "active";

    @Column(name = "all_prospect_lists")
    @Builder.Default
    private Boolean allProspectLists = false;

    @Column(name = "created_by")
    private UUID createdBy;

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
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
