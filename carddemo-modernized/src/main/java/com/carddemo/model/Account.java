package com.carddemo.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Account entity - modernized from COBOL copybook CVACT01Y (ACCOUNT-RECORD).
 *
 * Legacy COBOL layout (RECLN 300):
 *   ACCT-ID                 PIC 9(11)       -> accountId (String, 11 digits)
 *   ACCT-ACTIVE-STATUS      PIC X(01)       -> activeStatus
 *   ACCT-CURR-BAL           PIC S9(10)V99   -> currentBalance
 *   ACCT-CREDIT-LIMIT       PIC S9(10)V99   -> creditLimit
 *   ACCT-CASH-CREDIT-LIMIT  PIC S9(10)V99   -> cashCreditLimit
 *   ACCT-OPEN-DATE          PIC X(10)       -> openDate
 *   ACCT-EXPIRAION-DATE     PIC X(10)       -> expirationDate
 *   ACCT-REISSUE-DATE       PIC X(10)       -> reissueDate
 *   ACCT-CURR-CYC-CREDIT    PIC S9(10)V99   -> currentCycleCredit
 *   ACCT-CURR-CYC-DEBIT     PIC S9(10)V99   -> currentCycleDebit
 *   ACCT-ADDR-ZIP           PIC X(10)       -> addressZip
 *   ACCT-GROUP-ID           PIC X(10)       -> groupId
 *
 * Legacy data store: VSAM KSDS (ACCTDAT)
 * Modernized to: Cassandra table + JDBC relational backup
 */
@Table("accounts")
public class Account {

    @PrimaryKey("account_id")
    private String accountId;

    @Column("active_status")
    private String activeStatus;

    @Column("current_balance")
    private BigDecimal currentBalance;

    @Column("credit_limit")
    private BigDecimal creditLimit;

    @Column("cash_credit_limit")
    private BigDecimal cashCreditLimit;

    @Column("open_date")
    private LocalDate openDate;

    @Column("expiration_date")
    private LocalDate expirationDate;

    @Column("reissue_date")
    private LocalDate reissueDate;

    @Column("current_cycle_credit")
    private BigDecimal currentCycleCredit;

    @Column("current_cycle_debit")
    private BigDecimal currentCycleDebit;

    @Column("address_zip")
    private String addressZip;

    @Column("group_id")
    private String groupId;

    public Account() {}

    public Account(String accountId, String activeStatus, BigDecimal currentBalance,
                   BigDecimal creditLimit, BigDecimal cashCreditLimit, LocalDate openDate,
                   LocalDate expirationDate, LocalDate reissueDate, BigDecimal currentCycleCredit,
                   BigDecimal currentCycleDebit, String addressZip, String groupId) {
        this.accountId = accountId;
        this.activeStatus = activeStatus;
        this.currentBalance = currentBalance;
        this.creditLimit = creditLimit;
        this.cashCreditLimit = cashCreditLimit;
        this.openDate = openDate;
        this.expirationDate = expirationDate;
        this.reissueDate = reissueDate;
        this.currentCycleCredit = currentCycleCredit;
        this.currentCycleDebit = currentCycleDebit;
        this.addressZip = addressZip;
        this.groupId = groupId;
    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public void setCashCreditLimit(BigDecimal cashCreditLimit) { this.cashCreditLimit = cashCreditLimit; }
    public LocalDate getOpenDate() { return openDate; }
    public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public LocalDate getReissueDate() { return reissueDate; }
    public void setReissueDate(LocalDate reissueDate) { this.reissueDate = reissueDate; }
    public BigDecimal getCurrentCycleCredit() { return currentCycleCredit; }
    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) { this.currentCycleCredit = currentCycleCredit; }
    public BigDecimal getCurrentCycleDebit() { return currentCycleDebit; }
    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) { this.currentCycleDebit = currentCycleDebit; }
    public String getAddressZip() { return addressZip; }
    public void setAddressZip(String addressZip) { this.addressZip = addressZip; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
}
