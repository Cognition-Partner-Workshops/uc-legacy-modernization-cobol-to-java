package com.carddemo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "card_xref")
public class CardXref {
    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;
    @Column(name = "cust_id", precision = 9, scale = 0, nullable = false)
    private BigDecimal custId;
    @Column(name = "acct_id", precision = 11, scale = 0, nullable = false)
    private BigDecimal acctId;

    protected CardXref() {
    }

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public BigDecimal getCustId() { return custId; }
    public void setCustId(BigDecimal custId) { this.custId = custId; }
    public BigDecimal getAcctId() { return acctId; }
    public void setAcctId(BigDecimal acctId) { this.acctId = acctId; }
}
