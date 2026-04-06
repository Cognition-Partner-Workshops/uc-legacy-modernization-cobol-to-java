package com.suitecrm.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fp_events", schema = "event_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FPEvent {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_start")
    private LocalDateTime dateStart;

    @Column(name = "date_end")
    private LocalDateTime dateEnd;

    @Column(name = "duration_hours")
    private Integer durationHours;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(length = 50)
    private String status;

    @Column(name = "budget", precision = 26, scale = 6)
    private BigDecimal budget;

    @Column(name = "currency_id")
    private UUID currencyId;

    @Column(name = "invite_templates")
    private String inviteTemplates;

    @Column(name = "accept_redirect")
    private String acceptRedirect;

    @Column(name = "decline_redirect")
    private String declineRedirect;

    @Column(name = "location_id")
    private UUID locationId;

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
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
