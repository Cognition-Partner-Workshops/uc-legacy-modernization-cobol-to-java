package com.suitecrm.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_invitees", schema = "event_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInvitee {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "invitee_type", length = 50)
    private String inviteeType;

    @Column(name = "invitee_id")
    private UUID inviteeId;

    @Column(name = "email")
    private String email;

    @Column(name = "accept_status", length = 50)
    private String acceptStatus;

    @Column(name = "invite_sent")
    private Boolean inviteSent;

    @Column(name = "invite_sent_date")
    private LocalDateTime inviteSentDate;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (deleted == null) deleted = false;
        if (acceptStatus == null) acceptStatus = "none";
        if (inviteSent == null) inviteSent = false;
    }
}
