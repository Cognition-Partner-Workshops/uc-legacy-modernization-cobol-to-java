package com.suitecrm.casemodule.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cases", schema = "case_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupportCase {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "case_number", unique = true) private Integer caseNumber;
    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "type", length = 50) private String type;
    @Column(name = "status", length = 50) private String status;
    @Column(name = "state", length = 50) private String state;
    @Column(name = "priority", length = 50) private String priority;
    @Column(name = "resolution", columnDefinition = "TEXT") private String resolution;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "internal") @Builder.Default private Boolean internal = false;
    @Column(name = "suggestion_box", columnDefinition = "TEXT") private String suggestionBox;
    @Column(name = "update_text", columnDefinition = "TEXT") private String updateText;
    @Column(name = "account_id") private UUID accountId;
    @Column(name = "account_name", length = 255) private String accountName;
    @Column(name = "contact_created_by_id") private UUID contactCreatedById;
    @Column(name = "contact_created_by_name", length = 255) private String contactCreatedByName;
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
