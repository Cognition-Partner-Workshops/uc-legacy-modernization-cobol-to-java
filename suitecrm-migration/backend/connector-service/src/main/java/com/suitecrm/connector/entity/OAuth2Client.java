package com.suitecrm.connector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "oauth2_clients", schema = "connector_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OAuth2Client {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "client_id", nullable = false, length = 255) private String clientId;
    @Column(name = "client_secret", length = 512) private String clientSecret;
    @Column(name = "redirect_url", length = 512) private String redirectUrl;
    @Column(name = "allowed_grant_type", length = 100) private String allowedGrantType;
    @Column(name = "is_confidential") @Builder.Default private Boolean isConfidential = true;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
