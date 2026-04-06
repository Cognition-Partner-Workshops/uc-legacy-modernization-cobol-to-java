package com.suitecrm.cases.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "bugs", schema = "cases_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Bug {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "bug_number", length = 50) private String bugNumber;
    @Column(length = 100) private String status;
    @Column(length = 100) private String priority;
    @Column(length = 100) private String type;
    @Column(length = 100) private String category;
    @Column(name = "found_in_release", length = 255) private String foundInRelease;
    @Column(name = "fixed_in_release", length = 255) private String fixedInRelease;
    @Column(length = 100) private String resolution;
    @Column(name = "source", length = 100) private String source;
    @Column(name = "product_category", length = 100) private String productCategory;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "work_log", columnDefinition = "TEXT") private String workLog;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
