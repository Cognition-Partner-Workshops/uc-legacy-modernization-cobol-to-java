package com.suitecrm.connector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "external_oauth_providers", schema = "connector_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExternalOAuthProvider {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "type", length = 100) private String type;
    @Column(name = "connector_name", length = 255) private String connectorName;
    @Column(name = "client_id", length = 255) private String clientId;
    @Column(name = "client_secret", length = 512) private String clientSecret;
    @Column(name = "scope", length = 512) private String scope;
    @Column(name = "url_authorize", length = 512) private String urlAuthorize;
    @Column(name = "url_access_token", length = 512) private String urlAccessToken;
    @Column(name = "url_user_info", length = 512) private String urlUserInfo;
    @Column(name = "extra_provider_params", columnDefinition = "TEXT") private String extraProviderParams;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
