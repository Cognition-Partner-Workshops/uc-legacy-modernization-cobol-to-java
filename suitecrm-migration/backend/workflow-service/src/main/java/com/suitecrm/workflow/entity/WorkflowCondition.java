package com.suitecrm.workflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_conditions", schema = "workflow_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowCondition {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "module_name", length = 100)
    private String moduleName;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "value", length = 500)
    private String value;

    @Column(name = "value_type", length = 50)
    private String valueType;

    @Column(name = "order_num")
    private Integer orderNum;

    @Column(name = "date_entered", nullable = false, updatable = false)
    private LocalDateTime dateEntered;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
    }
}
