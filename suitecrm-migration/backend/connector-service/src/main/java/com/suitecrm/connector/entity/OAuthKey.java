package com.suitecrm.connector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "oauth_keys", schema = "connector_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthKey {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "connector_id")
    private UUID connectorId;

    @Column(name = "consumer_key")
    private String consumerKey;

    @Column(name = "consumer_secret")
    private String consumerSecret;

    @Column(name = "oauth_type", length = 20)
    private String oauthType;

    @Column(name = "token_url")
    private String tokenUrl;

    @Column(name = "authorize_url")
    private String authorizeUrl;

    @Column(name = "scope")
    private String scope;

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
