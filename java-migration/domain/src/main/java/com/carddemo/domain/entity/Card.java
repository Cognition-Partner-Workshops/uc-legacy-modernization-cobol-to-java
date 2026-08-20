package com.carddemo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "card")
public class Card {
    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;
    @Column(name = "acct_id", precision = 11, scale = 0, nullable = false)
    private BigDecimal acctId;
    @Column(name = "cvv_cd", precision = 3, scale = 0)
    private BigDecimal cvvCd;
    @Column(name = "embossed_name", length = 50)
    private String embossedName;
    @Column(name = "expiration_date", length = 10)
    private String expirationDate;
    @Column(name = "active_status", length = 1)
    private String activeStatus;

    protected Card() {
    }

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public BigDecimal getAcctId() { return acctId; }
    public void setAcctId(BigDecimal acctId) { this.acctId = acctId; }
    public BigDecimal getCvvCd() { return cvvCd; }
    public void setCvvCd(BigDecimal cvvCd) { this.cvvCd = cvvCd; }
    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
