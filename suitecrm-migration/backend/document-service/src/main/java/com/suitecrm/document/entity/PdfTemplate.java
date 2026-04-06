package com.suitecrm.document.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pdf_templates", schema = "document_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfTemplate {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "type", length = 100)
    private String type;

    @Column(name = "module", length = 100)
    private String module;

    @Column(name = "margin_left")
    @Builder.Default
    private Integer marginLeft = 15;

    @Column(name = "margin_right")
    @Builder.Default
    private Integer marginRight = 15;

    @Column(name = "margin_top")
    @Builder.Default
    private Integer marginTop = 16;

    @Column(name = "margin_bottom")
    @Builder.Default
    private Integer marginBottom = 16;

    @Column(name = "margin_header")
    @Builder.Default
    private Integer marginHeader = 9;

    @Column(name = "margin_footer")
    @Builder.Default
    private Integer marginFooter = 9;

    @Column(name = "page_size", length = 50)
    @Builder.Default
    private String pageSize = "A4";

    @Column(name = "orientation", length = 50)
    @Builder.Default
    private String orientation = "Portrait";

    @Column(name = "header", columnDefinition = "TEXT")
    private String header;

    @Column(name = "footer", columnDefinition = "TEXT")
    private String footer;

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
