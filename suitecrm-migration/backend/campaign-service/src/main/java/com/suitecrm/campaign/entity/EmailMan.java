package com.suitecrm.campaign.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "emailman", schema = "campaign_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmailMan {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(name = "campaign_id") private UUID campaignId;
    @Column(name = "marketing_id") private UUID marketingId;
    @Column(name = "list_id") private UUID listId;
    @Column(name = "related_id") private UUID relatedId;
    @Column(name = "related_type", length = 100) private String relatedType;
    @Column(name = "send_date_time") private LocalDateTime sendDateTime;
    @Column(name = "in_queue") @Builder.Default private Boolean inQueue = true;
    @Column(name = "in_queue_date") private LocalDateTime inQueueDate;
    @Column(name = "send_attempts") @Builder.Default private Integer sendAttempts = 0;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { if (inQueueDate == null) inQueueDate = LocalDateTime.now(); }
}
