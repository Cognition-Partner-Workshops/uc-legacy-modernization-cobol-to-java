package com.suitecrm.workflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_workflows", schema = "workflow_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedWorkflow {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "bean_id", nullable = false)
    private UUID beanId;

    @Column(name = "bean_module", length = 100)
    private String beanModule;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "Complete";

    @Column(name = "date_processed")
    private LocalDateTime dateProcessed;

    @Column(name = "date_entered", nullable = false, updatable = false)
    private LocalDateTime dateEntered;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (dateProcessed == null) dateProcessed = LocalDateTime.now();
    }
}
