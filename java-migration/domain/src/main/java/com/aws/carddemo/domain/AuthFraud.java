package com.aws.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "authfrds")
public class AuthFraud {
  @EmbeddedId AuthFraudId id;

  @Column(name = "auth_type", length = 4)
  String authType;

  @Column(name = "card_expiry_date", length = 4)
  String cardExpiryDate;

  @Column(name = "message_type", length = 6)
  String messageType;

  @Column(name = "message_source", length = 6)
  String messageSource;

  @Column(name = "auth_id_code", length = 6)
  String authIdCode;

  @Column(name = "auth_resp_code", length = 2)
  String authRespCode;

  @Column(name = "auth_resp_reason", length = 4)
  String authRespReason;

  @Column(name = "processing_code", length = 6)
  String processingCode;

  @Column(name = "transaction_amt", precision = 12, scale = 2)
  BigDecimal transactionAmt;

  @Column(name = "approved_amt", precision = 12, scale = 2)
  BigDecimal approvedAmt;

  @Column(name = "merchant_catagory_code", length = 4)
  String merchantCatagoryCode;

  @Column(name = "acqr_country_code", length = 3)
  String acqrCountryCode;

  @Column(name = "pos_entry_mode")
  Short posEntryMode;

  @Column(name = "merchant_id", length = 15)
  String merchantId;

  @Column(name = "merchant_name", length = 22)
  String merchantName;

  @Column(name = "merchant_city", length = 13)
  String merchantCity;

  @Column(name = "merchant_state", length = 2)
  String merchantState;

  @Column(name = "merchant_zip", length = 9)
  String merchantZip;

  @Column(name = "transaction_id", length = 15)
  String transactionId;

  @Column(name = "match_status", length = 1)
  String matchStatus;

  @Column(name = "auth_fraud", length = 1)
  String authFraud;

  @Column(name = "fraud_rpt_date")
  LocalDate fraudRptDate;

  @Column(name = "acct_id")
  Long acctId;

  @Column(name = "cust_id")
  Integer custId;

  public AuthFraudId getId() {
    return id;
  }

  public void setId(AuthFraudId value) {
    id = value;
  }

  public String getAuthType() {
    return authType;
  }

  public void setAuthType(String value) {
    authType = value;
  }

  public String getCardExpiryDate() {
    return cardExpiryDate;
  }

  public void setCardExpiryDate(String value) {
    cardExpiryDate = value;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String value) {
    messageType = value;
  }

  public String getMessageSource() {
    return messageSource;
  }

  public void setMessageSource(String value) {
    messageSource = value;
  }

  public String getAuthIdCode() {
    return authIdCode;
  }

  public void setAuthIdCode(String value) {
    authIdCode = value;
  }

  public String getAuthRespCode() {
    return authRespCode;
  }

  public void setAuthRespCode(String value) {
    authRespCode = value;
  }

  public String getAuthRespReason() {
    return authRespReason;
  }

  public void setAuthRespReason(String value) {
    authRespReason = value;
  }

  public String getProcessingCode() {
    return processingCode;
  }

  public void setProcessingCode(String value) {
    processingCode = value;
  }

  public BigDecimal getTransactionAmt() {
    return transactionAmt;
  }

  public void setTransactionAmt(BigDecimal value) {
    transactionAmt = value;
  }

  public BigDecimal getApprovedAmt() {
    return approvedAmt;
  }

  public void setApprovedAmt(BigDecimal value) {
    approvedAmt = value;
  }

  public String getMerchantCatagoryCode() {
    return merchantCatagoryCode;
  }

  public void setMerchantCatagoryCode(String value) {
    merchantCatagoryCode = value;
  }

  public String getAcqrCountryCode() {
    return acqrCountryCode;
  }

  public void setAcqrCountryCode(String value) {
    acqrCountryCode = value;
  }

  public Short getPosEntryMode() {
    return posEntryMode;
  }

  public void setPosEntryMode(Short value) {
    posEntryMode = value;
  }

  public String getMerchantId() {
    return merchantId;
  }

  public void setMerchantId(String value) {
    merchantId = value;
  }

  public String getMerchantName() {
    return merchantName;
  }

  public void setMerchantName(String value) {
    merchantName = value;
  }

  public String getMerchantCity() {
    return merchantCity;
  }

  public void setMerchantCity(String value) {
    merchantCity = value;
  }

  public String getMerchantState() {
    return merchantState;
  }

  public void setMerchantState(String value) {
    merchantState = value;
  }

  public String getMerchantZip() {
    return merchantZip;
  }

  public void setMerchantZip(String value) {
    merchantZip = value;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String value) {
    transactionId = value;
  }

  public String getMatchStatus() {
    return matchStatus;
  }

  public void setMatchStatus(String value) {
    matchStatus = value;
  }

  public String getAuthFraud() {
    return authFraud;
  }

  public void setAuthFraud(String value) {
    authFraud = value;
  }

  public LocalDate getFraudRptDate() {
    return fraudRptDate;
  }

  public void setFraudRptDate(LocalDate value) {
    fraudRptDate = value;
  }

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
}
