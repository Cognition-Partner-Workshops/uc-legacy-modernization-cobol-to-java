package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Card entity - converted from COBOL copybook CVACT02Y.cpy
 * Original VSAM record length: 150 bytes
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "card_acct_id")
    private Long cardAcctId;

    @Column(name = "card_cvv_cd")
    private Integer cvvCode;

    @Column(name = "card_embossed_name", length = 50)
    private String embossedName;

    @Column(name = "card_expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "card_active_status", length = 1)
    private String activeStatus;

    public Card() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public Long getCardAcctId() { return cardAcctId; }
    public void setCardAcctId(Long cardAcctId) { this.cardAcctId = cardAcctId; }

    public Integer getCvvCode() { return cvvCode; }
    public void setCvvCode(Integer cvvCode) { this.cvvCode = cvvCode; }

    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
