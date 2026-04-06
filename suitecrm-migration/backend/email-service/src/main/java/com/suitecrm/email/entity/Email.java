package com.suitecrm.email;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "emails", schema = "email_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Email {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "from_addr")
    private String fromAddr;

    @Column(name = "from_name")
    private String fromName;

    @Column(name = "to_addrs", columnDefinition = "TEXT")
    private String toAddrs;

    @Column(name = "cc_addrs", columnDefinition = "TEXT")
    private String ccAddrs;

    @Column(name = "bcc_addrs", columnDefinition = "TEXT")
    private String bccAddrs;

    @Column(name = "reply_to_addr")
    private String replyToAddr;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_html", columnDefinition = "TEXT")
    private String descriptionHtml;

    @Column(length = 50)
    private String type;

    @Column(length = 50)
    private String status;

    @Column(length = 50)
    private String intent;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "parent_type", length = 100)
    private String parentType;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "date_sent")
    private LocalDateTime dateSent;

    @Column(name = "flagged")
    private Boolean flagged;

    @Column(name = "reply_to_status")
    private Boolean replyToStatus;

    @Column(name = "mailbox_id")
    private UUID mailboxId;

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
        if (flagged == null) flagged = false;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
