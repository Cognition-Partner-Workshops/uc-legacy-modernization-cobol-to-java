package com.suitecrm.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meeting_invitees", schema = "calendar_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MeetingInvitee {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "meeting_id", nullable = false)
    private UUID meetingId;
    @Column(name = "invitee_id", nullable = false)
    private UUID inviteeId;
    @Column(name = "invitee_type", length = 50)
    private String inviteeType;
    @Column(name = "accept_status", length = 50)
    private String acceptStatus;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;
    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() { dateEntered = LocalDateTime.now(); }
}
