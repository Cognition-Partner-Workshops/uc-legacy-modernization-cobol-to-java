package com.suitecrm.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "trackers", schema = "notification_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Tracker {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "module_name", length = 100) private String moduleName;
    @Column(name = "item_id") private UUID itemId;
    @Column(name = "item_summary", length = 255) private String itemSummary;
    @Column(name = "action", length = 100) private String action;
    @Column(name = "session_id", length = 255) private String sessionId;
    @Column(name = "visible") @Builder.Default private Boolean visible = true;
    @Column(name = "date_modified", nullable = false) private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
