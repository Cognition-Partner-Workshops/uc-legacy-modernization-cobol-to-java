package com.suitecrm.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "calendar_accounts", schema = "calendar_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CalendarAccount {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "type", length = 50) private String type;
    @Column(name = "url", length = 500) private String url;
    @Column(name = "username", length = 255) private String username;
    @Column(name = "password", length = 255) private String password;
    @Column(name = "auth_type", length = 50) private String authType;
    @Column(name = "oauth_connection_id") private UUID oauthConnectionId;
    @Column(name = "sync_enabled") @Builder.Default private Boolean syncEnabled = true;
    @Column(name = "sync_interval") @Builder.Default private Integer syncInterval = 15;
    @Column(name = "last_sync") private LocalDateTime lastSync;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
