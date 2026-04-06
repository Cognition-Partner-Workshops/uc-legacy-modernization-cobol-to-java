package com.suitecrm.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dashlets", schema = "auth_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Dashlet {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "dashboard_id", nullable = false) private UUID dashboardId;
    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "dashlet_type", length = 100) private String dashletType;
    @Column(name = "dashlet_module", length = 100) private String dashletModule;
    @Column(name = "title", length = 255) private String title;
    @Column(name = "options", columnDefinition = "TEXT") private String options;
    @Column(name = "position") private Integer position;
    @Column(name = "column_index") @Builder.Default private Integer columnIndex = 0;
    @Column(name = "row_index") @Builder.Default private Integer rowIndex = 0;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
