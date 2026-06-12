package com.carddemo.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapping the COBOL CARD-XREF-RECORD from copybook CVACT03Y.cpy.
 *
 * <pre>
 * COBOL Field              -> Java Field
 * XREF-CARD-NUM  PIC X(16) -> cardNumber (String)
 * XREF-CUST-ID   PIC 9(09) -> customerId (Long)
 * XREF-ACCT-ID   PIC 9(11) -> accountId (Long)
 * </pre>
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    public CardXref() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
