package com.suitecrm.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contracts", schema = "account_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "renewal_reminder_date")
    private LocalDate renewalReminderDate;

    @Column(name = "customer_signed_date")
    private LocalDate customerSignedDate;

    @Column(name = "company_signed_date")
    private LocalDate companySignedDate;

    @Column(name = "total_contract_value")
    private BigDecimal totalContractValue;

    @Column(name = "currency_id")
    private UUID currencyId;

    @Column(name = "contract_type", length = 50)
    private String contractType;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "opportunity_id")
    private UUID opportunityId;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "date_entered", nullable = false, updatable = false)
    private LocalDateTime dateEntered;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        dateModified = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
