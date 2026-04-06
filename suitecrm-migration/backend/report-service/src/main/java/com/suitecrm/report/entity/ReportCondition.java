package com.suitecrm.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_conditions", schema = "report_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportCondition {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "report_id", nullable = false) private UUID reportId;
    @Column(name = "name", length = 255) private String name;
    @Column(name = "field", length = 100) private String field;
    @Column(name = "module_path", length = 255) private String modulePath;
    @Column(name = "operator", length = 50) private String operator;
    @Column(name = "value_type", length = 50) private String valueType;
    @Column(name = "value", columnDefinition = "TEXT") private String value;
    @Column(name = "logic_op", length = 10) @Builder.Default private String logicOp = "AND";
    @Column(name = "parenthesis", length = 20) private String parenthesis;
    @Column(name = "parameter") @Builder.Default private Boolean parameter = false;
    @Column(name = "condition_order") private Integer conditionOrder;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
