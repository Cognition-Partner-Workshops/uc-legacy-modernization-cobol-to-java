package com.suitecrm.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_templates", schema = "project_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TaskTemplate {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_template_id", nullable = false) private UUID projectTemplateId;
    @Column(nullable = false, length = 255) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "task_number") private Integer taskNumber;
    @Column private Integer duration;
    @Column(name = "duration_unit", length = 20) private String durationUnit;
    @Column(name = "estimated_effort") private BigDecimal estimatedEffort;
    @Column private BigDecimal utilization;
    @Column(name = "milestone_flag") @Builder.Default private Boolean milestoneFlag = false;
    @Column(name = "predecessors", length = 500) private String predecessors;
    @Column(name = "relationship_type", length = 50) private String relationshipType;
    @Column(name = "order_number") private Integer orderNumber;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
