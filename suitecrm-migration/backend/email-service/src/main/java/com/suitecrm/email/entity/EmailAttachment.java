package com.suitecrm.email;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_attachments", schema = "email_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email_id", nullable = false)
    private UUID emailId;

    @Column(name = "filename")
    private String filename;

    @Column(name = "file_mime_type")
    private String fileMimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_source", length = 50)
    private String fileSource;

    @Column(name = "file_ext", length = 50)
    private String fileExt;

    @Column(name = "storage_location")
    private String storageLocation;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (deleted == null) deleted = false;
    }
}
