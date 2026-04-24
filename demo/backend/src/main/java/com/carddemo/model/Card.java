package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped from COBOL copybook CVACT02Y.cpy (Card Record, RECLN 150).
 *
 * COBOL layout:
 *   05 CARD-NUM               PIC X(16)
 *   05 CARD-ACCT-ID           PIC 9(11)
 *   05 CARD-CVV-CD            PIC 9(03)
 *   05 CARD-EMBOSSED-NAME     PIC X(50)
 *   05 CARD-EXPIRAION-DATE    PIC X(10)
 *   05 CARD-ACTIVE-STATUS     PIC X(01)
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @Column(name = "card_number", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "account_id", length = 11, nullable = false)
    private String accountId;

    @Column(name = "cvv_code", length = 3)
    private String cvvCode;

    @Column(name = "embossed_name", length = 50)
    private String embossedName;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "active_status", length = 1)
    private String activeStatus;

    public Card() {}

    public Card(String cardNumber, String accountId, String cvvCode,
                String embossedName, String expirationDate, String activeStatus) {
        this.cardNumber = cardNumber;
        this.accountId = accountId;
        this.cvvCode = cvvCode;
        this.embossedName = embossedName;
        this.expirationDate = expirationDate;
        this.activeStatus = activeStatus;
    }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getCvvCode() { return cvvCode; }
    public void setCvvCode(String cvvCode) { this.cvvCode = cvvCode; }

    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
