package com.suitecrm.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "oauth2_auth_codes", schema = "auth_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OAuth2AuthCode {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, length = 500) private String code;
    @Column(name = "client_id", nullable = false, length = 255) private String clientId;
    @Column(name = "user_id") private UUID userId;
    @Column(name = "redirect_uri", length = 500) private String redirectUri;
    @Column(name = "scopes", length = 500) private String scopes;
    @Column(name = "expires_at", nullable = false) private LocalDateTime expiresAt;
    @Column(name = "revoked") @Builder.Default private Boolean revoked = false;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); }
}
