package com.suitecrm.kb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kb_documents", schema = "kb_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KBDocument {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "filename")
    private String filename;

    @Column(name = "file_mime_type")
    private String fileMimeType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "kb_content_id")
    private UUID kbContentId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(length = 50)
    private String status;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        dateModified = LocalDateTime.now();
        if (deleted == null) deleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
