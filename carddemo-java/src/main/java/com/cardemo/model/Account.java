package com.cardemo.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVACT01Y (ACCOUNT-RECORD).
 * Represents an account entity (record length 300).
 */
public class Account {

    private long accountId;                   // ACCT-ID PIC 9(11)
    private String activeStatus;              // ACCT-ACTIVE-STATUS PIC X(01)
    private BigDecimal currentBalance;        // ACCT-CURR-BAL PIC S9(10)V99
    private BigDecimal creditLimit;           // ACCT-CREDIT-LIMIT PIC S9(10)V99
    private BigDecimal cashCreditLimit;       // ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
    private String openDate;                  // ACCT-OPEN-DATE PIC X(10)
    private String expirationDate;            // ACCT-EXPIRAION-DATE PIC X(10)
    private String reissueDate;               // ACCT-REISSUE-DATE PIC X(10)
    private BigDecimal currentCycleCredit;    // ACCT-CURR-CYC-CREDIT PIC S9(10)V99
    private BigDecimal currentCycleDebit;     // ACCT-CURR-CYC-DEBIT PIC S9(10)V99
    private String addressZip;                // ACCT-ADDR-ZIP PIC X(10)
    private String groupId;                   // ACCT-GROUP-ID PIC X(10)

    public Account() {
        this.currentBalance = BigDecimal.ZERO;
        this.creditLimit = BigDecimal.ZERO;
        this.cashCreditLimit = BigDecimal.ZERO;
        this.currentCycleCredit = BigDecimal.ZERO;
        this.currentCycleDebit = BigDecimal.ZERO;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCashCreditLimit() {
        return cashCreditLimit;
    }

    public void setCashCreditLimit(BigDecimal cashCreditLimit) {
        this.cashCreditLimit = cashCreditLimit;
    }

    public String getOpenDate() {
        return openDate;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getReissueDate() {
        return reissueDate;
    }

    public void setReissueDate(String reissueDate) {
        this.reissueDate = reissueDate;
    }

    public BigDecimal getCurrentCycleCredit() {
        return currentCycleCredit;
    }

    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) {
        this.currentCycleCredit = currentCycleCredit;
    }

    public BigDecimal getCurrentCycleDebit() {
        return currentCycleDebit;
    }

    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) {
        this.currentCycleDebit = currentCycleDebit;
    }

    public String getAddressZip() {
        return addressZip;
    }

    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
