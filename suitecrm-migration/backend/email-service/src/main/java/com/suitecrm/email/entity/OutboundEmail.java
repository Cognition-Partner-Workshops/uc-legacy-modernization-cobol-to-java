package com.suitecrm.email.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbound_email", schema = "email_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboundEmail {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "type", length = 50) private String type;
    @Column(name = "mail_sendtype", length = 50) @Builder.Default private String mailSendtype = "SMTP";
    @Column(name = "mail_smtptype", length = 50) private String mailSmtptype;
    @Column(name = "mail_smtpserver", length = 255) private String mailSmtpserver;
    @Column(name = "mail_smtpport") @Builder.Default private Integer mailSmtpport = 587;
    @Column(name = "mail_smtpuser", length = 255) private String mailSmtpuser;
    @Column(name = "mail_smtppass", length = 255) private String mailSmtppass;
    @Column(name = "mail_smtpauth_req") @Builder.Default private Boolean mailSmtpauthReq = true;
    @Column(name = "mail_smtpssl") @Builder.Default private Boolean mailSmtpssl = false;
    @Column(name = "mail_smtptls") @Builder.Default private Boolean mailSmtptls = true;
    @Column(name = "auth_type", length = 50) private String authType;
    @Column(name = "external_oauth_connection_id") private UUID externalOauthConnectionId;
    @Column(name = "user_id") private UUID userId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "modified_user_id") private UUID modifiedUserId;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
