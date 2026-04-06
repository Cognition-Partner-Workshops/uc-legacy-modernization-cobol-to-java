package com.suitecrm.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meetings_users", schema = "activity_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MeetingUser {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "meeting_id", nullable = false) private UUID meetingId;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "required") @Builder.Default private Boolean required = true;
    @Column(name = "accept_status", length = 25) @Builder.Default private String acceptStatus = "none";
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
}
