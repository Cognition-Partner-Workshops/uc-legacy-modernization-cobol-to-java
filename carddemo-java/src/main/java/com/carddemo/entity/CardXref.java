package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "card_xrefs")
public class CardXref {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "acct_id")
    private Long acctId;

    public CardXref() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public Long getCustId() { return custId; }
    public void setCustId(Long custId) { this.custId = custId; }

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
}
