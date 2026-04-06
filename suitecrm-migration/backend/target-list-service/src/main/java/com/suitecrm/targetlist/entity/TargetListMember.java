package com.suitecrm.targetlist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prospect_list_members", schema = "target_list_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetListMember {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "prospect_list_id", nullable = false)
    private UUID prospectListId;

    @Column(name = "related_id", nullable = false)
    private UUID relatedId;

    @Column(name = "related_type", length = 50, nullable = false)
    private String relatedType;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (deleted == null) deleted = false;
    }
}
