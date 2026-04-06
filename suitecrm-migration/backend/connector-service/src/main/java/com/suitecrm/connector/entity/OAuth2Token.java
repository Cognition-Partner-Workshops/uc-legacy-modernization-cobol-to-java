package com.suitecrm.connector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "oauth2_tokens", schema = "connector_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OAuth2Token {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(name = "client_id", nullable = false) private UUID clientId;
    @Column(name = "user_id") private UUID userId;
    @Column(name = "access_token", nullable = false, length = 512) private String accessToken;
    @Column(name = "refresh_token", length = 512) private String refreshToken;
    @Column(name = "token_type", length = 50) private String tokenType;
    @Column(name = "access_token_expires") private LocalDateTime accessTokenExpires;
    @Column(name = "refresh_token_expires") private LocalDateTime refreshTokenExpires;
    @Column(name = "grant_type", length = 100) private String grantType;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); }
}
