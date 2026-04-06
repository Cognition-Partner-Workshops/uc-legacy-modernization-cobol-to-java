package com.suitecrm.casemodule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bugs", schema = "case_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bug {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "bug_number", unique = true) private Integer bugNumber;
    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "type", length = 50) private String type;
    @Column(name = "status", length = 50) private String status;
    @Column(name = "priority", length = 50) private String priority;
    @Column(name = "resolution", columnDefinition = "TEXT") private String resolution;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "work_log", columnDefinition = "TEXT") private String workLog;
    @Column(name = "source", length = 100) private String source;
    @Column(name = "product_category", length = 100) private String productCategory;
    @Column(name = "found_in_release", length = 255) private String foundInRelease;
    @Column(name = "found_in_release_id") private UUID foundInReleaseId;
    @Column(name = "fixed_in_release", length = 255) private String fixedInRelease;
    @Column(name = "fixed_in_release_id") private UUID fixedInReleaseId;
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
