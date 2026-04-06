package com.suitecrm.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports", schema = "report_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "report_type", length = 50)
    private String reportType;

    @Column(name = "module_name", length = 100)
    private String moduleName;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "chart_type", length = 50)
    private String chartType;

    @Column(name = "schedule_type", length = 50)
    private String scheduleType;

    @Column(name = "favorite")
    @Builder.Default
    private Boolean favorite = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

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
