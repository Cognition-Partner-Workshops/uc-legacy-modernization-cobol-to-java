package com.aws.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "pending_auth_summary")
public class PendingAuthSummary {
  @Id
  @Column(name = "acct_id")
  Long acctId;

  @Column(name = "cust_id")
  Integer custId;

  @Column(name = "auth_status", length = 1)
  String authStatus;

  @Column(name = "account_status", length = 2, nullable = false)
  String accountStatus;

  @Column(name = "credit_limit", precision = 11, scale = 2)
  BigDecimal creditLimit;

  @Column(name = "cash_limit", precision = 11, scale = 2)
  BigDecimal cashLimit;

  @Column(name = "credit_balance", precision = 11, scale = 2)
  BigDecimal creditBalance;

  @Column(name = "cash_balance", precision = 11, scale = 2)
  BigDecimal cashBalance;

  @Column(name = "approved_auth_count", nullable = false)
  Integer approvedAuthCount;

  @Column(name = "declined_auth_count", nullable = false)
  Integer declinedAuthCount;

  @Column(name = "approved_auth_amount", precision = 11, scale = 2)
  BigDecimal approvedAuthAmount;

  @Column(name = "declined_auth_amount", precision = 11, scale = 2)
  BigDecimal declinedAuthAmount;

  public Long getAcctId() {
    return acctId;
  }

  public void setAcctId(Long value) {
    acctId = value;
  }

  public Integer getCustId() {
    return custId;
  }

  public void setCustId(Integer value) {
    custId = value;
  }

  public String getAuthStatus() {
    return authStatus;
  }

  public void setAuthStatus(String value) {
    authStatus = value;
  }

  public String getAccountStatus() {
    return accountStatus;
  }

  public void setAccountStatus(String value) {
    accountStatus = value;
  }

  public BigDecimal getCreditLimit() {
    return creditLimit;
  }

  public void setCreditLimit(BigDecimal value) {
    creditLimit = value;
  }

  public BigDecimal getCashLimit() {
    return cashLimit;
  }

  public void setCashLimit(BigDecimal value) {
    cashLimit = value;
  }

  public BigDecimal getCreditBalance() {
    return creditBalance;
  }

  public void setCreditBalance(BigDecimal value) {
    creditBalance = value;
  }

  public BigDecimal getCashBalance() {
    return cashBalance;
  }

  public void setCashBalance(BigDecimal value) {
    cashBalance = value;
  }

  public Integer getApprovedAuthCount() {
    return approvedAuthCount;
  }

  public void setApprovedAuthCount(Integer value) {
    approvedAuthCount = value;
  }

  public Integer getDeclinedAuthCount() {
    return declinedAuthCount;
  }

  public void setDeclinedAuthCount(Integer value) {
    declinedAuthCount = value;
  }

  public BigDecimal getApprovedAuthAmount() {
    return approvedAuthAmount;
  }

  public void setApprovedAuthAmount(BigDecimal value) {
    approvedAuthAmount = value;
  }

  public BigDecimal getDeclinedAuthAmount() {
    return declinedAuthAmount;
  }

  public void setDeclinedAuthAmount(BigDecimal value) {
    declinedAuthAmount = value;
  }
}
