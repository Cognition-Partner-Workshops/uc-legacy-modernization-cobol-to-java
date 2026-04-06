package com.suitecrm.workflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_actions", schema = "workflow_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkflowAction {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "workflow_id", nullable = false) private UUID workflowId;
    @Column(name = "name", length = 255) private String name;
    @Column(name = "action", length = 100) private String action;
    @Column(name = "action_order") private Integer actionOrder;
    @Column(name = "parameters", columnDefinition = "TEXT") private String parameters;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
