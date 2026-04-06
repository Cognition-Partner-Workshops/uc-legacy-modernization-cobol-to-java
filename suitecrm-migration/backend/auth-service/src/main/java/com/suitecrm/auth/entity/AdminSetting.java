package com.suitecrm.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_settings", schema = "auth_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminSetting {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "category", nullable = false, length = 100) private String category;
    @Column(name = "name", nullable = false, length = 100) private String name;
    @Column(name = "value", columnDefinition = "TEXT") private String value;
    @Column(name = "platform", length = 50) @Builder.Default private String platform = "default";
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
