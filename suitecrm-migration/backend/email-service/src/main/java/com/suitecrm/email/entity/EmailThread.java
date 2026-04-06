package com.suitecrm.email;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_threads", schema = "email_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailThread {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String subject;

    @Column(name = "last_message_date")
    private LocalDateTime lastMessageDate;

    @Column(name = "message_count")
    private Integer messageCount;

    @Column(name = "participant_ids", columnDefinition = "TEXT")
    private String participantIds;

    @Column(name = "related_module", length = 100)
    private String relatedModule;

    @Column(name = "related_module_id")
    private UUID relatedModuleId;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (deleted == null) deleted = false;
        if (messageCount == null) messageCount = 0;
    }
}
