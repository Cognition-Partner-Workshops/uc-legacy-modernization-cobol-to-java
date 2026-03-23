package com.carddemo.dto;

import java.math.BigDecimal;

/**
 * Account update request DTO - replaces COBOL COACTUPC screen input.
 * Legacy: COACTUPC program receives account update data via BMS map COACTUP
 */
public class AccountUpdateRequest {

    private String activeStatus;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private String addressZip;
    private String groupId;

    public AccountUpdateRequest() {}

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public void setCashCreditLimit(BigDecimal cashCreditLimit) { this.cashCreditLimit = cashCreditLimit; }
    public String getAddressZip() { return addressZip; }
    public void setAddressZip(String addressZip) { this.addressZip = addressZip; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
}
