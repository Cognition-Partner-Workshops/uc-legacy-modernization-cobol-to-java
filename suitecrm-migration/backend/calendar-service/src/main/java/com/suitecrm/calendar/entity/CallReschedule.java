package com.suitecrm.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "calls_reschedule", schema = "calendar_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CallReschedule {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "call_id", nullable = false)
    private UUID callId;
    @Column(length = 100)
    private String reason;
    @Column(name = "old_date_start")
    private LocalDateTime oldDateStart;
    @Column(name = "new_date_start")
    private LocalDateTime newDateStart;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;
    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() { dateEntered = LocalDateTime.now(); }
}
