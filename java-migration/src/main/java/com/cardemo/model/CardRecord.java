/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Card entity - migrated from COBOL copybook CVACT02Y.cpy.
 * Original COBOL record length: 150 bytes.
 */
@Entity
@Table(name = "cards")
public class CardRecord {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "card_acct_id")
    private long cardAcctId;

    @Column(name = "card_cvv_cd")
    private int cardCvvCd;

    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    @Column(name = "card_expiration_date", length = 10)
    private String cardExpirationDate;

    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;

    public CardRecord() {
    }

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
