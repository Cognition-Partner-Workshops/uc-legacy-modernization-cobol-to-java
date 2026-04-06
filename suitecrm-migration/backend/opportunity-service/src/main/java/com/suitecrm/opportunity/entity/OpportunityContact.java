package com.suitecrm.opportunity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "opportunities_contacts", schema = "opportunity_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OpportunityContact {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "opportunity_id", nullable = false) private UUID opportunityId;
    @Column(name = "contact_id", nullable = false) private UUID contactId;
    @Column(name = "contact_role", length = 50) private String contactRole;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
}
