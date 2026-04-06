package com.suitecrm.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log", schema = "auth_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "parent_id", nullable = false) private UUID parentId;
    @Column(name = "parent_module", nullable = false, length = 100) private String parentModule;
    @Column(name = "field_name", nullable = false, length = 100) private String fieldName;
    @Column(name = "data_type", length = 50) private String dataType;
    @Column(name = "before_value_string", columnDefinition = "TEXT") private String beforeValueString;
    @Column(name = "after_value_string", columnDefinition = "TEXT") private String afterValueString;
    @Column(name = "before_value_text", columnDefinition = "TEXT") private String beforeValueText;
    @Column(name = "after_value_text", columnDefinition = "TEXT") private String afterValueText;
    @Column(name = "changed_by") private UUID changedBy;
    @Column(name = "date_created", nullable = false, updatable = false) private LocalDateTime dateCreated;

    @PrePersist protected void onCreate() { dateCreated = LocalDateTime.now(); }
}
