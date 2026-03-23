package com.carddemo.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Card Cross-Reference entity - modernized from COBOL copybook CVACT03Y (CARD-XREF-RECORD).
 *
 * Legacy COBOL layout (RECLN 50):
 *   XREF-CARD-NUM    PIC X(16)   -> cardNumber
 *   XREF-CUST-ID     PIC 9(09)   -> customerId
 *   XREF-ACCT-ID     PIC 9(11)   -> accountId
 *
 * Legacy data store: VSAM KSDS (CARDXREF) with Alternate Index (CXACAIX)
 * Modernized to: Cassandra table (links cards, customers, and accounts)
 */
@Table("card_cross_references")
public class CardCrossReference {

    @PrimaryKey("card_number")
    private String cardNumber;

    @Column("customer_id")
    private String customerId;

    @Column("account_id")
    private String accountId;

    public CardCrossReference() {}

    public CardCrossReference(String cardNumber, String customerId, String accountId) {
        this.cardNumber = cardNumber;
        this.customerId = customerId;
        this.accountId = accountId;
    }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
}
