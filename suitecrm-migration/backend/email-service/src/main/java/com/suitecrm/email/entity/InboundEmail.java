package com.suitecrm.email;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inbound_email", schema = "email_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundEmail {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String status;

    @Column(name = "server_url")
    private String serverUrl;

    @Column(name = "email_user")
    private String emailUser;

    @Column(name = "email_password")
    private String emailPassword;

    @Column(length = 10)
    private String port;

    @Column(length = 50)
    private String protocol;

    @Column(name = "mailbox_type", length = 50)
    private String mailboxType;

    @Column(name = "service", length = 50)
    private String service;

    @Column(name = "is_personal")
    private Boolean isPersonal;

    @Column(name = "is_ssl")
    private Boolean isSsl;

    @Column(name = "delete_seen")
    private Boolean deleteSeen;

    @Column(name = "mailbox")
    private String mailbox;

    @Column(name = "group_id")
    private UUID groupId;

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
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
