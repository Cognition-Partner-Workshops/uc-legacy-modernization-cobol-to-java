package com.suitecrm.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "pdf_templates", schema = "template_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PdfTemplate {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "type", length = 100) private String type;
    @Column(name = "module_name", length = 100) private String moduleName;
    @Column(name = "pdfheader", columnDefinition = "TEXT") private String pdfHeader;
    @Column(name = "pdffooter", columnDefinition = "TEXT") private String pdfFooter;
    @Column(name = "body", columnDefinition = "TEXT") private String body;
    @Column(name = "margin_left") private Integer marginLeft;
    @Column(name = "margin_right") private Integer marginRight;
    @Column(name = "margin_top") private Integer marginTop;
    @Column(name = "margin_bottom") private Integer marginBottom;
    @Column(name = "margin_header") private Integer marginHeader;
    @Column(name = "margin_footer") private Integer marginFooter;
    @Column(name = "page_size", length = 50) private String pageSize;
    @Column(name = "orientation", length = 50) private String orientation;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
