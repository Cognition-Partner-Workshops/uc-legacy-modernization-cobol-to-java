package com.aws.carddemo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "card_xref")
public class CardXref {
  @EmbeddedId CardXrefId id;

  @Column(name = "cust_id", nullable = false)
  Integer custId;

  @Column(name = "acct_id", nullable = false)
  Long acctId;

  public CardXrefId getId() {
    return id;
  }

  public void setId(CardXrefId v) {
    id = v;
  }

  public Integer getCustId() {
    return custId;
  }

  public void setCustId(Integer v) {
    custId = v;
  }

  public Long getAcctId() {
    return acctId;
  }

  public void setAcctId(Long v) {
    acctId = v;
  }
}
