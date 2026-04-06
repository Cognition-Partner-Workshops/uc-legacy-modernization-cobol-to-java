package com.suitecrm.quotes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contracts", schema = "quotes_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Contract {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(length = 100)
    private String status;

    @Column(name = "contract_type", length = 100)
    private String contractType;

    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "renewal_reminder_date")
    private LocalDate renewalReminderDate;

    @Column(name = "total_contract_value", precision = 26, scale = 6)
    private BigDecimal totalContractValue;

    @Column(name = "currency_id")
    private UUID currencyId;
    @Column(name = "account_id")
    private UUID accountId;
    @Column(name = "contact_id")
    private UUID contactId;
    @Column(name = "opportunity_id")
    private UUID opportunityId;
    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;
    @Column(name = "date_modified")
    private LocalDateTime dateModified;
    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
