package com.suitecrm.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_fields", schema = "report_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportField {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "module_name", length = 100)
    private String moduleName;

    @Column(name = "label", length = 255)
    private String label;

    @Column(name = "field_function", length = 50)
    private String fieldFunction;

    @Column(name = "sort_order", length = 10)
    private String sortOrder;

    @Column(name = "group_by")
    @Builder.Default
    private Boolean groupBy = false;

    @Column(name = "display")
    @Builder.Default
    private Boolean display = true;

    @Column(name = "order_num")
    private Integer orderNum;

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
