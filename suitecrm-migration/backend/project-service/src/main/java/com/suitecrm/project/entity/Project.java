package com.suitecrm.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "projects", schema = "project_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "Draft";

    @Column(name = "priority", length = 50)
    @Builder.Default
    private String priority = "Medium";

    @Column(name = "estimated_start_date")
    private LocalDate estimatedStartDate;

    @Column(name = "estimated_end_date")
    private LocalDate estimatedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost")
    private BigDecimal actualCost;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "override_business_hours")
    @Builder.Default
    private Boolean overrideBusinessHours = false;

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
