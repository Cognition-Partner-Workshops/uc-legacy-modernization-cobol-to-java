package com.aws.carddemo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "card")
public class Card {
  @Id
  @Column(name = "card_num", length = 16)
  String cardNum;

  @Column(name = "acct_id")
  Long acctId;

  @Column(name = "cvv_cd")
  Integer cvvCd;

  @Column(name = "embossed_name", length = 50)
  String embossedName;

  @Column(name = "expiraion_date", length = 10)
  String expiraionDate;

  @Column(name = "active_status", length = 1)
  String activeStatus;

  public String getCardNum() {
    return cardNum;
  }

  public void setCardNum(String v) {
    cardNum = v;
  }

  public Long getAcctId() {
    return acctId;
  }

  public void setAcctId(Long v) {
    acctId = v;
  }

  public Integer getCvvCd() {
    return cvvCd;
  }

  public void setCvvCd(Integer v) {
    cvvCd = v;
  }

  public String getEmbossedName() {
    return embossedName;
  }

  public void setEmbossedName(String v) {
    embossedName = v;
  }

  public String getExpiraionDate() {
    return expiraionDate;
  }

  public void setExpiraionDate(String v) {
    expiraionDate = v;
  }

  public String getActiveStatus() {
    return activeStatus;
  }

  public void setActiveStatus(String v) {
    activeStatus = v;
  }
}
