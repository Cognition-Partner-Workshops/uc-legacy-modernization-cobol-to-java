package com.suitecrm.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "resource_calendars", schema = "scheduler_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ResourceCalendar {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "user_id") private UUID userId;
    @Column(name = "event_date") private LocalDate eventDate;
    @Column(name = "event_type", length = 100) private String eventType;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "is_available") @Builder.Default private Boolean isAvailable = true;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
