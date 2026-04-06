package com.suitecrm.document.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_revisions", schema = "document_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRevision {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "revision", nullable = false)
    private Integer revision;

    @Column(name = "filename", length = 255)
    private String filename;

    @Column(name = "file_mime_type", length = 100)
    private String fileMimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_ext", length = 20)
    private String fileExt;

    @Column(name = "s3_key", length = 500)
    private String s3Key;

    @Column(name = "s3_bucket", length = 255)
    private String s3Bucket;

    @Column(name = "change_log", columnDefinition = "TEXT")
    private String changeLog;

    @Column(name = "created_by")
    private UUID createdBy;

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
