package com.carddemo.qa.model;

/**
 * Java representation of the CVACT02Y copybook (CARD-RECORD).
 * Models the credit card entity (record length 150 bytes).
 */
public class CardRecord {

    private String cardNumber;       // CARD-NUM              PIC X(16)
    private long accountId;          // CARD-ACCT-ID          PIC 9(11)
    private int cvvCode;             // CARD-CVV-CD           PIC 9(03)
    private String embossedName;     // CARD-EMBOSSED-NAME    PIC X(50)
    private String expirationDate;   // CARD-EXPIRAION-DATE   PIC X(10)
    private String activeStatus;     // CARD-ACTIVE-STATUS    PIC X(01)

    public CardRecord() {
    }

    public CardRecord(String cardNumber, long accountId, int cvvCode,
                      String embossedName, String expirationDate, String activeStatus) {
        this.cardNumber = cardNumber;
        this.accountId = accountId;
        this.cvvCode = cvvCode;
        this.embossedName = embossedName;
        this.expirationDate = expirationDate;
        this.activeStatus = activeStatus;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getCvvCode() {
        return cvvCode;
    }

    public void setCvvCode(int cvvCode) {
        this.cvvCode = cvvCode;
    }

    public String getEmbossedName() {
        return embossedName;
    }

    public void setEmbossedName(String embossedName) {
        this.embossedName = embossedName;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public boolean isActive() {
        return "Y".equals(activeStatus);
    }
}
