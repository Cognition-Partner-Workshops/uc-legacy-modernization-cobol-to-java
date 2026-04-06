package com.suitecrm.email.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "emails", schema = "email_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Email {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", length = 255) private String name;
    @Column(name = "subject", length = 500) private String subject;
    @Column(name = "date_sent_received") private LocalDateTime dateSentReceived;
    @Column(name = "type", length = 50) private String type;
    @Column(name = "status", length = 50) private String status;
    @Column(name = "intent", length = 50) private String intent;
    @Column(name = "flagged") @Builder.Default private Boolean flagged = false;
    @Column(name = "reply_to_status") @Builder.Default private Boolean replyToStatus = false;
    @Column(name = "from_addr_name", length = 500) private String fromAddrName;
    @Column(name = "reply_to_addr", length = 500) private String replyToAddr;
    @Column(name = "to_addrs_names", columnDefinition = "TEXT") private String toAddrsNames;
    @Column(name = "cc_addrs_names", columnDefinition = "TEXT") private String ccAddrsNames;
    @Column(name = "bcc_addrs_names", columnDefinition = "TEXT") private String bccAddrsNames;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "description_html", columnDefinition = "TEXT") private String descriptionHtml;
    @Column(name = "raw_source", columnDefinition = "TEXT") private String rawSource;
    @Column(name = "message_id", length = 255) private String messageId;
    @Column(name = "uid", length = 255) private String uid;
    @Column(name = "msgno") private Integer msgno;
    @Column(name = "imap_keywords", length = 500) private String imapKeywords;
    @Column(name = "is_imported") @Builder.Default private Boolean isImported = false;
    @Column(name = "is_only_plain_text") @Builder.Default private Boolean isOnlyPlainText = false;
    @Column(name = "has_attachment") @Builder.Default private Boolean hasAttachment = false;
    @Column(name = "orphaned") @Builder.Default private Boolean orphaned = false;
    @Column(name = "opt_in", length = 50) private String optIn;
    @Column(name = "folder", length = 255) private String folder;
    @Column(name = "folder_type", length = 50) private String folderType;
    @Column(name = "category_id", length = 100) private String categoryId;
    @Column(name = "mailbox_id") private UUID mailboxId;
    @Column(name = "parent_id") private UUID parentId;
    @Column(name = "parent_type", length = 100) private String parentType;
    @Column(name = "parent_name", length = 255) private String parentName;
    @Column(name = "last_synced") private LocalDateTime lastSynced;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "modified_user_id") private UUID modifiedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
    @Version @Column(name = "version") private Long version;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
