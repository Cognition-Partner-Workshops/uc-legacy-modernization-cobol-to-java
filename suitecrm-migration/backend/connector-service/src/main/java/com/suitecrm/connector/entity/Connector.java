package com.suitecrm.connector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "connectors", schema = "connector_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Connector {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "connector_type", length = 50)
    private String connectorType;

    @Column(name = "source_module", length = 100)
    private String sourceModule;

    @Column(name = "connector_class")
    private String connectorClass;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "api_version", length = 20)
    private String apiVersion;

    @Column(name = "auth_type", length = 50)
    private String authType;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "field_mapping", columnDefinition = "TEXT")
    private String fieldMapping;

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
        if (isEnabled == null) isEnabled = true;
        if (status == null) status = "active";
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
