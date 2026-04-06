package com.suitecrm.targetlist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prospect_lists_prospects", schema = "targetlist_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProspectListProspect {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "prospect_list_id", nullable = false) private UUID prospectListId;
    @Column(name = "related_id", nullable = false) private UUID relatedId;
    @Column(name = "related_type", nullable = false, length = 100) private String relatedType;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
}
