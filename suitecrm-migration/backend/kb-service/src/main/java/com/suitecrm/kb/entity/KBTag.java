package com.suitecrm.kb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kb_tags", schema = "kb_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KBTag {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "tag_type", length = 50)
    private String tagType;

    @Column(name = "usage_count")
    private Integer usageCount;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (deleted == null) deleted = false;
        if (usageCount == null) usageCount = 0;
    }
}
