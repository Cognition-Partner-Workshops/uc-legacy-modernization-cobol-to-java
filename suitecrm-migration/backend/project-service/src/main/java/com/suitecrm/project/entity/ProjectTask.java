package com.suitecrm.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_tasks", schema = "project_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProjectTask {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false) private UUID projectId;
    @Column(nullable = false, length = 255) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "task_number") private Integer taskNumber;
    @Column(length = 50) private String status;
    @Column(length = 50) private String priority;
    @Column(name = "percent_complete") private BigDecimal percentComplete;
    @Column(name = "milestone_flag") @Builder.Default private Boolean milestoneFlag = false;
    @Column(name = "date_start") private LocalDate dateStart;
    @Column(name = "date_finish") private LocalDate dateFinish;
    @Column(name = "date_due") private LocalDate dateDue;
    @Column private Integer duration;
    @Column(name = "duration_unit", length = 20) private String durationUnit;
    @Column(name = "actual_duration") private Integer actualDuration;
    @Column(name = "estimated_effort") private BigDecimal estimatedEffort;
    @Column(name = "actual_effort") private BigDecimal actualEffort;
    @Column private BigDecimal utilization;
    @Column(name = "parent_task_id") private UUID parentTaskId;
    @Column(name = "predecessors", length = 500) private String predecessors;
    @Column(name = "order_number") private Integer orderNumber;
    @Column(name = "relationship_type", length = 50) private String relationshipType;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "modified_user_id") private UUID modifiedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default private Boolean deleted = false;
    @Version @Column(name = "version") private Long version;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
