package com.suitecrm.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "scheduler_jobs", schema = "scheduler_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SchedulerJob {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "scheduler_id") private UUID schedulerId;
    @Column(name = "execute_time") private LocalDateTime executeTime;
    @Column(length = 100) private String status;
    @Column(length = 100) private String resolution;
    @Column(name = "message", columnDefinition = "TEXT") private String message;
    @Column(name = "target", length = 255) private String target;
    @Column(name = "data", columnDefinition = "TEXT") private String data;
    @Column(name = "retry_count") @Builder.Default private Integer retryCount = 0;
    @Column(name = "failure_count") @Builder.Default private Integer failureCount = 0;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
