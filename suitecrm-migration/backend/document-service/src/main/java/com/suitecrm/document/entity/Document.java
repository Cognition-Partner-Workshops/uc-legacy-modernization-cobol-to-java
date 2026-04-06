package com.suitecrm.document.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents", schema = "document_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "category_id", length = 100)
    private String categoryId;

    @Column(name = "subcategory_id", length = 100)
    private String subcategoryId;

    @Column(name = "status_id", length = 50)
    @Builder.Default
    private String statusId = "Active";

    @Column(name = "active_date")
    private LocalDate activeDate;

    @Column(name = "exp_date")
    private LocalDate expDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "template_type", length = 50)
    private String templateType;

    @Column(name = "is_template")
    @Builder.Default
    private Boolean isTemplate = false;

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
