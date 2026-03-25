package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA entity mapping for the Card VSAM record.
 * Maps to COBOL copybook: CVACT02Y.cpy (150-byte record)
 *
 * COBOL record layout:
 *   CARD-NUM             PIC X(16)
 *   CARD-ACCT-ID         PIC 9(11)
 *   CARD-CVV-CD          PIC 9(03)
 *   CARD-EMBOSSED-NAME   PIC X(50)
 *   CARD-EXPIRAION-DATE  PIC X(10)
 *   CARD-ACTIVE-STATUS   PIC X(01)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "cvv_code")
    private int cvvCode;

    @Column(name = "embossed_name", length = 50)
    private String embossedName;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "active_status", length = 1)
    private String activeStatus;
}
