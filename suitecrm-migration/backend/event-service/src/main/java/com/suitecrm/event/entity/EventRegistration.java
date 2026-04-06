package com.suitecrm.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_registrations", schema = "event_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistration {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "lead_id")
    private UUID leadId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "company")
    private String company;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "accept_status", length = 50)
    private String acceptStatus;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (registrationDate == null) registrationDate = LocalDateTime.now();
        if (deleted == null) deleted = false;
        if (status == null) status = "registered";
        if (acceptStatus == null) acceptStatus = "none";
    }
}
