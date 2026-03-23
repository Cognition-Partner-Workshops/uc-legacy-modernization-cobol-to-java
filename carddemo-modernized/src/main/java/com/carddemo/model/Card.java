package com.carddemo.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDate;

/**
 * Card entity - modernized from COBOL copybook CVACT02Y (CARD-RECORD).
 *
 * Legacy COBOL layout (RECLN 150):
 *   CARD-NUM               PIC X(16)   -> cardNumber
 *   CARD-ACCT-ID           PIC 9(11)   -> accountId
 *   CARD-CVV-CD            PIC 9(03)   -> cvvCode
 *   CARD-EMBOSSED-NAME     PIC X(50)   -> embossedName
 *   CARD-EXPIRAION-DATE    PIC X(10)   -> expirationDate
 *   CARD-ACTIVE-STATUS     PIC X(01)   -> activeStatus
 *
 * Legacy data store: VSAM KSDS (CARDDAT)
 * Modernized to: Cassandra table
 */
@Table("cards")
public class Card {

    @PrimaryKey("card_number")
    private String cardNumber;

    @Column("account_id")
    private String accountId;

    @Column("cvv_code")
    private String cvvCode;

    @Column("embossed_name")
    private String embossedName;

    @Column("expiration_date")
    private LocalDate expirationDate;

    @Column("active_status")
    private String activeStatus;

    public Card() {}

    public Card(String cardNumber, String accountId, String cvvCode,
                String embossedName, LocalDate expirationDate, String activeStatus) {
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
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
