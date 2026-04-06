package com.suitecrm.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_tasks", schema = "project_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTask {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "Not Started";

    @Column(name = "priority", length = 50)
    @Builder.Default
    private String priority = "Medium";

    @Column(name = "percent_complete")
    @Builder.Default
    private Integer percentComplete = 0;

    @Column(name = "task_number")
    private Integer taskNumber;

    @Column(name = "order_number")
    private Integer orderNumber;

    @Column(name = "estimated_effort")
    private Integer estimatedEffort;

    @Column(name = "actual_effort")
    private Integer actualEffort;

    @Column(name = "utilization")
    private Integer utilization;

    @Column(name = "date_start")
    private LocalDate dateStart;

    @Column(name = "date_finish")
    private LocalDate dateFinish;

    @Column(name = "date_due")
    private LocalDate dateDue;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "duration_unit", length = 20)
    @Builder.Default
    private String durationUnit = "Days";

    @Column(name = "parent_task_id")
    private UUID parentTaskId;

    @Column(name = "milestone_flag")
    @Builder.Default
    private Boolean milestoneFlag = false;

    @Column(name = "predecessors", length = 500)
    private String predecessors;

    @Column(name = "relationship_type", length = 50)
    private String relationshipType;

    @Column(name = "lag")
    private Integer lag;

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
