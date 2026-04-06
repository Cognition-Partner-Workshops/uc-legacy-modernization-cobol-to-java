package com.suitecrm.contact.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contacts_cases", schema = "contact_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactCase {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "contact_id", nullable = false) private UUID contactId;
    @Column(name = "case_id", nullable = false) private UUID caseId;
    @Column(name = "contact_role", length = 50) private String contactRole;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
}
