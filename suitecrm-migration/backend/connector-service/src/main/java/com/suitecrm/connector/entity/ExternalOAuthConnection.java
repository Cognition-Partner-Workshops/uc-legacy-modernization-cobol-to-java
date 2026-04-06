package com.suitecrm.connector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "external_oauth_connections", schema = "connector_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExternalOAuthConnection {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "provider_id") private UUID providerId;
    @Column(name = "client_id", length = 255) private String clientId;
    @Column(name = "client_secret", length = 512) private String clientSecret;
    @Column(name = "token_url", length = 512) private String tokenUrl;
    @Column(name = "authorize_url", length = 512) private String authorizeUrl;
    @Column(name = "scope", length = 512) private String scope;
    @Column(name = "redirect_uri", length = 512) private String redirectUri;
    @Column(name = "access_token", columnDefinition = "TEXT") private String accessToken;
    @Column(name = "refresh_token", columnDefinition = "TEXT") private String refreshToken;
    @Column(name = "token_expiry") private LocalDateTime tokenExpiry;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
