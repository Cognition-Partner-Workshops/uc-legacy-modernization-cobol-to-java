package com.suitecrm.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "alerts", schema = "notification_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Alert {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "alert_type", length = 100) private String alertType;
    @Column(name = "url_redirect", length = 512) private String urlRedirect;
    @Column(name = "target_module", length = 100) private String targetModule;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "is_read") @Builder.Default private Boolean isRead = false;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
