package com.cardemo.model;

/**
 * Java equivalent of COBOL copybook CVACT03Y (CARD-XREF-RECORD).
 * Represents card cross-reference record (record length 50).
 */
public class CardXref {

    private String cardNumber;    // XREF-CARD-NUM PIC X(16)
    private long customerId;      // XREF-CUST-ID PIC 9(09)
    private long accountId;       // XREF-ACCT-ID PIC 9(11)

    public CardXref() {
    }

    public CardXref(String cardNumber, long customerId, long accountId) {
        this.cardNumber = cardNumber;
        this.customerId = customerId;
        this.accountId = accountId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }
}
