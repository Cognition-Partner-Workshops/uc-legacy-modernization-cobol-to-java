package com.cardemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVACT02Y.cpy (Card record, RECLN 150).
 * Source: app/cpy/CVACT02Y.cpy
 *
 * TODO: Implement card validation from COCRDUPC.cbl (Card Update program)
 * TODO: Add relationship to Account entity via acctId FK
 */
@Entity
@Table(name = "credit_cards")
public class CreditCard {

    @Id
    @Column(name = "card_num", length = 16)
    private String cardNum;

    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    @Column(name = "cvv_cd", nullable = false)
    private Integer cvvCd;

    @Column(name = "embossed_name", length = 50)
    private String embossedName;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "active_status", length = 1, nullable = false)
    private String activeStatus;

    public CreditCard() {
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public Integer getCvvCd() {
        return cvvCd;
    }

    public void setCvvCd(Integer cvvCd) {
        this.cvvCd = cvvCd;
    }

    public String getEmbossedName() {
        return embossedName;
    }

    public void setEmbossedName(String embossedName) {
        this.embossedName = embossedName;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }
}
