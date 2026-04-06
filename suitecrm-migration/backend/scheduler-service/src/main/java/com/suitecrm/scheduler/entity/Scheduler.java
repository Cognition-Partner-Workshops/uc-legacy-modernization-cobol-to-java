package com.suitecrm.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "schedulers", schema = "scheduler_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Scheduler {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "job", length = 255) private String job;
    @Column(name = "job_interval", length = 100) private String jobInterval;
    @Column(name = "time_from") private String timeFrom;
    @Column(name = "time_to") private String timeTo;
    @Column(name = "last_run") private LocalDateTime lastRun;
    @Column(length = 100) private String status;
    @Column(name = "catch_up") @Builder.Default private Boolean catchUp = true;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
