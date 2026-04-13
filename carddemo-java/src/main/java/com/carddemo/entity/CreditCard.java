package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "credit_cards")
public class CreditCard {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "acct_id")
    private Long acctId;

    @Column(name = "cvv_code")
    private Integer cvvCode;

    @Column(name = "embossed_name", length = 50)
    private String embossedName;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "active_status", length = 1)
    private String activeStatus;

    public CreditCard() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }

    public Integer getCvvCode() { return cvvCode; }
    public void setCvvCode(Integer cvvCode) { this.cvvCode = cvvCode; }

    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
