package com.suitecrm.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "import_maps", schema = "auth_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImportMap {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "source", length = 100) private String source;
    @Column(name = "module", nullable = false, length = 100) private String module;
    @Column(name = "content", columnDefinition = "TEXT") private String content;
    @Column(name = "has_header") @Builder.Default private Boolean hasHeader = true;
    @Column(name = "delimiter", length = 5) @Builder.Default private String delimiter = ",";
    @Column(name = "enclosure", length = 5) @Builder.Default private String enclosure = """;
    @Column(name = "is_published") @Builder.Default private Boolean isPublished = false;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
