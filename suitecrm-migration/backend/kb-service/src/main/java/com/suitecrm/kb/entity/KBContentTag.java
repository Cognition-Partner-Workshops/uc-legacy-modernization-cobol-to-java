package com.suitecrm.kb.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kb_content_tags", schema = "kb_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KBContentTag {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "kb_content_id", nullable = false)
    private UUID kbContentId;

    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
    }
}
