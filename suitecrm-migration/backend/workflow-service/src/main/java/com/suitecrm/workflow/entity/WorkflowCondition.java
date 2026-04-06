package com.suitecrm.workflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_conditions", schema = "workflow_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkflowCondition {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "workflow_id", nullable = false) private UUID workflowId;
    @Column(name = "name", length = 255) private String name;
    @Column(name = "field", length = 100) private String field;
    @Column(name = "module_path", length = 255) private String modulePath;
    @Column(name = "operator", length = 50) private String operator;
    @Column(name = "value_type", length = 50) private String valueType;
    @Column(name = "value", columnDefinition = "TEXT") private String value;
    @Column(name = "condition_order") private Integer conditionOrder;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
