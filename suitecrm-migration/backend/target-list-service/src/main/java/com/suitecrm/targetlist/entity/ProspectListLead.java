package com.suitecrm.targetlist.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "prospect_list_leads", schema = "target_list_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProspectListLead {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(name = "prospect_list_id", nullable = false) private UUID prospectListId;
    @Column(name = "lead_id", nullable = false) private UUID leadId;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); }
}
