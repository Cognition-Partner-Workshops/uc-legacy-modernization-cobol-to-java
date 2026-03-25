package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping for the Card Cross-Reference VSAM record.
 * Maps to COBOL copybook: CVACT03Y.cpy (50-byte record)
 *
 * COBOL record layout:
 *   XREF-CARD-NUM   PIC X(16)
 *   XREF-CUST-ID    PIC 9(09)
 *   XREF-ACCT-ID    PIC 9(11)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card_cross_references")
public class CardCrossReference {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;
}
