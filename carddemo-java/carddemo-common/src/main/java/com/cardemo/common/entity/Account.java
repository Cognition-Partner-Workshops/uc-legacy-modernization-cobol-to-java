package com.cardemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity mapped from COBOL copybook CVACT01Y.cpy (Account record, RECLN 300).
 * Source: app/cpy/CVACT01Y.cpy
 *
 * TODO: Implement full validation rules from COACTUPC.cbl (Account Update program)
 * TODO: Add audit trail fields (created_at, updated_at) not present in COBOL
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "acct_id")
    private Long acctId;

    @Column(name = "active_status", length = 1, nullable = false)
    private String activeStatus;

    @Column(name = "curr_bal", precision = 12, scale = 2, nullable = false)
    private BigDecimal currBal = BigDecimal.ZERO;

    @Column(name = "credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "cash_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal cashCreditLimit = BigDecimal.ZERO;

    @Column(name = "open_date")
    private LocalDate openDate;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "reissue_date")
    private LocalDate reissueDate;

    @Column(name = "curr_cyc_credit", precision = 12, scale = 2, nullable = false)
    private BigDecimal currCycCredit = BigDecimal.ZERO;

    @Column(name = "curr_cyc_debit", precision = 12, scale = 2, nullable = false)
    private BigDecimal currCycDebit = BigDecimal.ZERO;

    @Column(name = "addr_zip", length = 10)
    private String addrZip;

    @Column(name = "group_id", length = 10)
    private String groupId;

    public Account() {
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public BigDecimal getCurrBal() {
        return currBal;
    }

    public void setCurrBal(BigDecimal currBal) {
        this.currBal = currBal;
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

    public LocalDate getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDate getReissueDate() {
        return reissueDate;
    }

    public void setReissueDate(LocalDate reissueDate) {
        this.reissueDate = reissueDate;
    }

    public BigDecimal getCurrCycCredit() {
        return currCycCredit;
    }

    public void setCurrCycCredit(BigDecimal currCycCredit) {
        this.currCycCredit = currCycCredit;
    }

    public BigDecimal getCurrCycDebit() {
        return currCycDebit;
    }

    public void setCurrCycDebit(BigDecimal currCycDebit) {
        this.currCycDebit = currCycDebit;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
