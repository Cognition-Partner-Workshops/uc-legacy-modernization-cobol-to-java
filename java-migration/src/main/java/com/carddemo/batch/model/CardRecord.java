package com.carddemo.batch.model;

/**
 * Java equivalent of COBOL copybook CVACT02Y - Card entity (RECLN 150).
 * Maps to: CARD-RECORD in COBOL.
 */
public class CardRecord {

    private String cardNum;             // PIC X(16)
    private long cardAcctId;            // PIC 9(11)
    private int cardCvvCd;              // PIC 9(03)
    private String cardEmbossedName;    // PIC X(50)
    private String cardExpirationDate;  // PIC X(10)
    private String cardActiveStatus;    // PIC X(01)

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public long getCardAcctId() {
        return cardAcctId;
    }

    public void setCardAcctId(long cardAcctId) {
        this.cardAcctId = cardAcctId;
    }

    public int getCardCvvCd() {
        return cardCvvCd;
    }

    public void setCardCvvCd(int cardCvvCd) {
        this.cardCvvCd = cardCvvCd;
    }

    public String getCardEmbossedName() {
        return cardEmbossedName;
    }

    public void setCardEmbossedName(String cardEmbossedName) {
        this.cardEmbossedName = cardEmbossedName;
    }

    public String getCardExpirationDate() {
        return cardExpirationDate;
    }

    public void setCardExpirationDate(String cardExpirationDate) {
        this.cardExpirationDate = cardExpirationDate;
    }

    public String getCardActiveStatus() {
        return cardActiveStatus;
    }

    public void setCardActiveStatus(String cardActiveStatus) {
        this.cardActiveStatus = cardActiveStatus;
    }

    @Override
    public String toString() {
        return "CardRecord{" +
                "cardNum='" + cardNum + '\'' +
                ", cardAcctId=" + cardAcctId +
                ", cardCvvCd=" + cardCvvCd +
                ", cardEmbossedName='" + cardEmbossedName + '\'' +
                ", cardExpirationDate='" + cardExpirationDate + '\'' +
                ", cardActiveStatus='" + cardActiveStatus + '\'' +
                '}';
    }
}
