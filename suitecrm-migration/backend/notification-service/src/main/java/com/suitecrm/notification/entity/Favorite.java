package com.suitecrm.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "favorites", schema = "notification_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Favorite {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(name = "module_name", nullable = false, length = 100) private String moduleName;
    @Column(name = "record_id", nullable = false) private UUID recordId;
    @Column(name = "assigned_user_id", nullable = false) private UUID assignedUserId;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); }
}
