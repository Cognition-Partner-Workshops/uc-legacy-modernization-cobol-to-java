package com.suitecrm.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "email_templates", schema = "template_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmailTemplate {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(columnDefinition = "TEXT") private String subject;
    @Column(name = "body", columnDefinition = "TEXT") private String body;
    @Column(name = "body_html", columnDefinition = "TEXT") private String bodyHtml;
    @Column(length = 100) private String type;
    @Column(name = "text_only") @Builder.Default private Boolean textOnly = false;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
