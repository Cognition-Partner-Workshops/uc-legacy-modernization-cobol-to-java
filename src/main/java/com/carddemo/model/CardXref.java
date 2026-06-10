package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "card_xref")
public class CardXref {

    @Id
    @Size(max = 16)
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "cust_id")
    private long custId;

    @Column(name = "acct_id")
    private long acctId;

    public CardXref() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public long getCustId() { return custId; }
    public void setCustId(long custId) { this.custId = custId; }

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardXref that)) return false;
        return Objects.equals(cardNum, that.cardNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNum);
    }
}
