package com.suitecrm.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_fields", schema = "report_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportField {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "report_id", nullable = false) private UUID reportId;
    @Column(name = "name", length = 255) private String name;
    @Column(name = "label", length = 255) private String label;
    @Column(name = "field", length = 100) private String field;
    @Column(name = "module_path", length = 255) private String modulePath;
    @Column(name = "field_function", length = 50) private String fieldFunction;
    @Column(name = "sort_by", length = 10) private String sortBy;
    @Column(name = "sort_order") private Integer sortOrder;
    @Column(name = "group_by") @Builder.Default private Boolean groupBy = false;
    @Column(name = "group_order") private Integer groupOrder;
    @Column(name = "group_display") @Builder.Default private Boolean groupDisplay = false;
    @Column(name = "field_order") private Integer fieldOrder;
    @Column(name = "display") @Builder.Default private Boolean display = true;
    @Column(name = "total") @Builder.Default private Boolean total = false;
    @Column(name = "format", length = 100) private String format;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
