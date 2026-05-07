package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped from COBOL copybook CVACT03Y.cpy (Card Cross-Reference, RECLN 50).
 *
 * COBOL layout:
 *   05 XREF-CARD-NUM   PIC X(16)
 *   05 XREF-CUST-ID    PIC 9(09)
 *   05 XREF-ACCT-ID    PIC 9(11)
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "customer_id", length = 9)
    private String customerId;

    @Column(name = "account_id", length = 11)
    private String accountId;

    public CardXref() {}

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
}
