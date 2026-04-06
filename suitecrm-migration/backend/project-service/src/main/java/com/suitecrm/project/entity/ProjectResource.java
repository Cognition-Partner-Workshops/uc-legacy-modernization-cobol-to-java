package com.suitecrm.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_resources", schema = "project_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResource {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "role", length = 100)
    private String role;

    @Column(name = "date_entered", nullable = false, updatable = false)
    private LocalDateTime dateEntered;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
    }
}
