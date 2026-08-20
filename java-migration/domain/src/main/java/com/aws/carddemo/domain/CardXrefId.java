package com.aws.carddemo.domain;

import jakarta.persistence.*;
import java.io.Serializable;

@Embeddable
public class CardXrefId implements Serializable {
  @Column(name = "card_num", length = 16)
  String cardNum;

  public CardXrefId() {}

  public CardXrefId(String cardNum) {
    this.cardNum = cardNum;
  }

  public String getCardNum() {
    return cardNum;
  }

  public void setCardNum(String v) {
    cardNum = v;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof CardXrefId k && java.util.Objects.equals(cardNum, k.cardNum);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(cardNum);
  }
}
