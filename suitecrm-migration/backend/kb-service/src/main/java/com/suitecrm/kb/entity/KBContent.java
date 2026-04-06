package com.suitecrm.kb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kb_contents", schema = "kb_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KBContent {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 50)
    private String status;

    @Column(name = "revision")
    private Integer revision;

    @Column(name = "active_date")
    private LocalDateTime activeDate;

    @Column(name = "exp_date")
    private LocalDateTime expDate;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "helpful_count")
    private Integer helpfulCount;

    @Column(name = "not_helpful_count")
    private Integer notHelpfulCount;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

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
        if (viewCount == null) viewCount = 0;
        if (helpfulCount == null) helpfulCount = 0;
        if (notHelpfulCount == null) notHelpfulCount = 0;
        if (revision == null) revision = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
