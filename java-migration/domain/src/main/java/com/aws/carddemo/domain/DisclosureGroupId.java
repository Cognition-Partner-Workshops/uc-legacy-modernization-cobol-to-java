package com.aws.carddemo.domain;

import jakarta.persistence.*;
import java.io.Serializable;

@Embeddable
public class DisclosureGroupId implements Serializable {
  @Column(name = "acct_group_id", length = 10)
  String acctGroupId;

  @Column(name = "tran_type_cd", length = 2)
  String tranTypeCd;

  @Column(name = "tran_cat_cd")
  Integer tranCatCd;

  public DisclosureGroupId() {}

  public DisclosureGroupId(String a, String t, Integer c) {
    acctGroupId = a;
    tranTypeCd = t;
    tranCatCd = c;
  }

  public String getAcctGroupId() {
    return acctGroupId;
  }

  public void setAcctGroupId(String v) {
    acctGroupId = v;
  }

  public String getTranTypeCd() {
    return tranTypeCd;
  }

  public void setTranTypeCd(String v) {
    tranTypeCd = v;
  }

  public Integer getTranCatCd() {
    return tranCatCd;
  }

  public void setTranCatCd(Integer v) {
    tranCatCd = v;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof DisclosureGroupId k
        && java.util.Objects.equals(acctGroupId, k.acctGroupId)
        && java.util.Objects.equals(tranTypeCd, k.tranTypeCd)
        && java.util.Objects.equals(tranCatCd, k.tranCatCd);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(acctGroupId, tranTypeCd, tranCatCd);
  }
}
