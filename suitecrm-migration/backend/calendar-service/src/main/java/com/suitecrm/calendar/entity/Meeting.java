package com.suitecrm.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meetings", schema = "calendar_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Meeting {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "date_start")
    private LocalDateTime dateStart;
    @Column(name = "date_end")
    private LocalDateTime dateEnd;

    @Column(length = 100)
    private String status;
    @Column(length = 100)
    private String type;
    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_hours")
    private Integer durationHours;
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "parent_type", length = 100)
    private String parentType;
    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "reminder_time")
    private Integer reminderTime;
    @Column(name = "email_reminder_time")
    private Integer emailReminderTime;

    @Column(name = "external_id", length = 255)
    private String externalId;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;
    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;
    @Column(name = "date_modified")
    private LocalDateTime dateModified;
    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
