package com.suitecrm.connector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "integration_mappings", schema = "connector_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationMapping {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "connector_id", nullable = false)
    private UUID connectorId;

    @Column(name = "source_module", length = 100)
    private String sourceModule;

    @Column(name = "source_field", length = 100)
    private String sourceField;

    @Column(name = "target_field", length = 100)
    private String targetField;

    @Column(name = "mapping_type", length = 50)
    private String mappingType;

    @Column(name = "transformation")
    private String transformation;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (deleted == null) deleted = false;
        if (isRequired == null) isRequired = false;
    }
}
