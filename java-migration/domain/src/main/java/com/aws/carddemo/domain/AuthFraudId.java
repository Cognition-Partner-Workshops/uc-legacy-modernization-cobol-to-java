package com.aws.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

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

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof AuthFraudId that)) return false;
    return Objects.equals(cardNum, that.cardNum) && Objects.equals(authTs, that.authTs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardNum, authTs);
  }
}
