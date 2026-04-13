package com.carddemo.entity;

import java.io.Serializable;
import java.util.Objects;

public class TransactionId implements Serializable {

    private String cardNum;
    private String tranId;

    public TransactionId() {}

    public TransactionId(String cardNum, String tranId) {
        this.cardNum = cardNum;
        this.tranId = tranId;
    }

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionId that = (TransactionId) o;
        return Objects.equals(cardNum, that.cardNum) && Objects.equals(tranId, that.tranId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardNum, tranId);
    }
}
