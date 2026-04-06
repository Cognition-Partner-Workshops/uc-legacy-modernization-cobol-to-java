package com.suitecrm.report.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_conditions", schema = "report_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCondition {

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

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "value", length = 500)
    private String value;

    @Column(name = "value_type", length = 50)
    private String valueType;

    @Column(name = "order_num")
    private Integer orderNum;

    @Column(name = "group_condition", length = 10)
    @Builder.Default
    private String groupCondition = "AND";

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
