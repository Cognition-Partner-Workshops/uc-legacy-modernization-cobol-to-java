package com.suitecrm.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_charts", schema = "report_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportChart {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "report_id", nullable = false) private UUID reportId;
    @Column(name = "name", length = 255) private String name;
    @Column(name = "type", length = 50) private String type;
    @Column(name = "x_field") private UUID xField;
    @Column(name = "y_field") private UUID yField;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
