package com.suitecrm.lead.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lead_conversions", schema = "lead_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadConversion {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "lead_id", nullable = false)
    private UUID leadId;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "opportunity_id")
    private UUID opportunityId;

    @Column(name = "converted_by")
    private UUID convertedBy;

    @Column(name = "conversion_date", nullable = false)
    private LocalDateTime conversionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        if (conversionDate == null) conversionDate = LocalDateTime.now();
    }
}
