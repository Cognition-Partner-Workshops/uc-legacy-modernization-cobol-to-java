package com.suitecrm.cases.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cases", schema = "case_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportCase {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "case_number", unique = true, nullable = false)
    private Long caseNumber;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "New";

    @Column(name = "priority", length = 50)
    @Builder.Default
    private String priority = "Medium";

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "work_log", columnDefinition = "TEXT")
    private String workLog;

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
