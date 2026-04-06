package com.suitecrm.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scheduled_reports", schema = "report_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledReport {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "schedule_type", length = 50)
    private String scheduleType;

    @Column(name = "time_interval", length = 50)
    private String timeInterval;

    @Column(name = "date_start")
    private LocalDateTime dateStart;

    @Column(name = "next_run")
    private LocalDateTime nextRun;

    @Column(name = "last_run")
    private LocalDateTime lastRun;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    @Column(name = "users", columnDefinition = "TEXT")
    private String users;

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
