package com.suitecrm.email;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbound_email", schema = "email_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundEmail {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String type;

    @Column(name = "mail_sendtype", length = 50)
    private String mailSendType;

    @Column(name = "mail_smtptype", length = 50)
    private String mailSmtpType;

    @Column(name = "mail_smtpserver")
    private String mailSmtpServer;

    @Column(name = "mail_smtpport")
    private Integer mailSmtpPort;

    @Column(name = "mail_smtpuser")
    private String mailSmtpUser;

    @Column(name = "mail_smtppass")
    private String mailSmtpPass;

    @Column(name = "mail_smtpauth_req")
    private Boolean mailSmtpAuthReq;

    @Column(name = "mail_smtpssl")
    private Boolean mailSmtpSsl;

    @Column(name = "user_id")
    private UUID userId;

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
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
