package com.suitecrm.email.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inbound_email", schema = "email_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InboundEmail {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "status", length = 50) private String status;
    @Column(name = "server_url", length = 255) private String serverUrl;
    @Column(name = "email_user", length = 255) private String emailUser;
    @Column(name = "email_password", length = 255) private String emailPassword;
    @Column(name = "port") private Integer port;
    @Column(name = "service", length = 50) private String service;
    @Column(name = "mailbox", length = 255) private String mailbox;
    @Column(name = "connection_string", length = 500) private String connectionString;
    @Column(name = "auth_type", length = 50) private String authType;
    @Column(name = "external_oauth_connection_id") private UUID externalOauthConnectionId;
    @Column(name = "protocol", length = 20) private String protocol;
    @Column(name = "is_ssl") @Builder.Default private Boolean isSsl = false;
    @Column(name = "is_personal") @Builder.Default private Boolean isPersonal = false;
    @Column(name = "is_default") @Builder.Default private Boolean isDefault = false;
    @Column(name = "mailbox_type", length = 50) private String mailboxType;
    @Column(name = "template_id") private UUID templateId;
    @Column(name = "group_id") private UUID groupId;
    @Column(name = "stored_options", columnDefinition = "TEXT") private String storedOptions;
    @Column(name = "distribution_method", length = 50) private String distributionMethod;
    @Column(name = "distribution_user_id") private UUID distributionUserId;
    @Column(name = "create_case_template_id") private UUID createCaseTemplateId;
    @Column(name = "auto_reply_template_id") private UUID autoReplyTemplateId;
    @Column(name = "auto_reply_max") @Builder.Default private Integer autoReplyMax = 10;
    @Column(name = "move_to_trash_folder") @Builder.Default private Boolean moveToTrashFolder = false;
    @Column(name = "mark_read") @Builder.Default private Boolean markRead = false;
    @Column(name = "only_since") @Builder.Default private Boolean onlySince = false;
    @Column(name = "filter_domain", length = 255) private String filterDomain;
    @Column(name = "allow_outbound_group_usage") @Builder.Default private Boolean allowOutboundGroupUsage = false;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "modified_user_id") private UUID modifiedUserId;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
