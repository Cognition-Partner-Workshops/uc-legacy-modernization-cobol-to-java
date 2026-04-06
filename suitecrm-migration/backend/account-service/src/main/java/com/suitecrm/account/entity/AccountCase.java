package com.suitecrm.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts_cases", schema = "account_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountCase {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "account_id", nullable = false) private UUID accountId;
    @Column(name = "case_id", nullable = false) private UUID caseId;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
}
