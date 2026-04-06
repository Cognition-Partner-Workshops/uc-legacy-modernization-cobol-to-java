package com.suitecrm.workflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflows", schema = "workflow_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Workflow {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "flow_module", length = 100) private String flowModule;
    @Column(name = "status", length = 50) private String status;
    @Column(name = "run_when", length = 50) private String runWhen;
    @Column(name = "run_on_import") @Builder.Default private Boolean runOnImport = false;
    @Column(name = "multiple_runs") @Builder.Default private Boolean multipleRuns = false;
    @Column(name = "flow_run_on", length = 50) private String flowRunOn;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "repeated_runs") @Builder.Default private Boolean repeatedRuns = false;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "modified_user_id") private UUID modifiedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
    @Version @Column(name = "version") private Long version;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
