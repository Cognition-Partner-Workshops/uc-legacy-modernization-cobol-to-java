package com.carddemo.batch.model;

/**
 * Java equivalent of COBOL copybook CVACT03Y - Card Cross-Reference Record (RECLN 50).
 *
 * Original COBOL layout:
 *   05 XREF-CARD-NUM  PIC X(16)
 *   05 XREF-CUST-ID   PIC 9(09)
 *   05 XREF-ACCT-ID   PIC 9(11)
 *   05 FILLER          PIC X(14)
 */
public class CardXrefRecord {

    private String cardNum;
    private long custId;
    private long acctId;

    public CardXrefRecord() {}

    public CardXrefRecord(String cardNum, long custId, long acctId) {
        this.cardNum = cardNum;
        this.custId = custId;
        this.acctId = acctId;
    }

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }

    public long getCustId() { return custId; }
    public void setCustId(long custId) { this.custId = custId; }

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    @Override
    public String toString() {
        return "CardXrefRecord{cardNum='" + cardNum + '\'' +
               ", custId=" + custId +
               ", acctId=" + acctId +
               '}';
    }
}
