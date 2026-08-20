package com.aws.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;

@Embeddable
public class AuthFraudId implements Serializable {
  @Column(name = "card_num", length = 16)
  String cardNum;

  @Column(name = "auth_ts")
  LocalDateTime authTs;

  public AuthFraudId() {}

  public AuthFraudId(String cardNum, LocalDateTime authTs) {
    this.cardNum = cardNum;
    this.authTs = authTs;
  }

  public String getCardNum() {
    return cardNum;
  }

  public void setCardNum(String value) {
    cardNum = value;
  }

  public LocalDateTime getAuthTs() {
    return authTs;
  }

  public void setAuthTs(LocalDateTime value) {
    authTs = value;
  }
}
