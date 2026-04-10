package com.carddemo.card.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "card")
public class Card {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "card_acct_id")
    private Long cardAcctId;

    @Column(name = "card_cvv_cd")
    private Short cardCvvCd;

    @Column(name = "card_embossed_name", length = 50)
    private String cardEmbossedName;

    @Column(name = "card_expiraion_date", length = 10)
    private String cardExpiraionDate;

    @Column(name = "card_active_status", length = 1)
    private String cardActiveStatus;

    public Card() {}

    public String getCardNum() { return cardNum; }
    public void setCardNum(String cardNum) { this.cardNum = cardNum; }
    public Long getCardAcctId() { return cardAcctId; }
    public void setCardAcctId(Long cardAcctId) { this.cardAcctId = cardAcctId; }
    public Short getCardCvvCd() { return cardCvvCd; }
    public void setCardCvvCd(Short cardCvvCd) { this.cardCvvCd = cardCvvCd; }
    public String getCardEmbossedName() { return cardEmbossedName; }
    public void setCardEmbossedName(String n) { this.cardEmbossedName = n; }
    public String getCardExpiraionDate() { return cardExpiraionDate; }
    public void setCardExpiraionDate(String d) { this.cardExpiraionDate = d; }
    public String getCardActiveStatus() { return cardActiveStatus; }
    public void setCardActiveStatus(String s) { this.cardActiveStatus = s; }
}
