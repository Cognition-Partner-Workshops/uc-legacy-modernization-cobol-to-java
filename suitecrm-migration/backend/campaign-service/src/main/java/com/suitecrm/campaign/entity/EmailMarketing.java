package com.suitecrm.campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "email_marketing", schema = "campaign_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmailMarketing {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "campaign_id") private UUID campaignId;
    @Column(name = "template_id") private UUID templateId;
    @Column(name = "from_addr", length = 255) private String fromAddr;
    @Column(name = "from_name", length = 255) private String fromName;
    @Column(name = "reply_to_name", length = 255) private String replyToName;
    @Column(name = "reply_to_addr", length = 255) private String replyToAddr;
    @Column(name = "inbound_email_id") private UUID inboundEmailId;
    @Column(name = "date_start") private LocalDateTime dateStart;
    @Column(length = 100) private String status;
    @Column(name = "all_prospect_lists") @Builder.Default private Boolean allProspectLists = false;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
