package com.suitecrm.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "permissions", schema = "auth_schema",
        uniqueConstraints = @UniqueConstraint(columnNames = {"module_name", "action_name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @Column(name = "action_name", nullable = false, length = 50)
    private String actionName;

    @Column(name = "access_level", nullable = false)
    private Integer accessLevel;

    @Column(name = "description", length = 255)
    private String description;
}
