package com.carddemo.filematch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * JPA entity mapping the COBOL ACCOUNT-RECORD copybook (CVACT01Y.cpy).
 *
 * COBOL layout (RECLN 300):
 *   05 ACCT-ID                  PIC 9(11)
 *   05 ACCT-ACTIVE-STATUS       PIC X(01)
 *   05 ACCT-CURR-BAL            PIC S9(10)V99
 *   05 ACCT-CREDIT-LIMIT        PIC S9(10)V99
 *   05 ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99
 *   05 ACCT-OPEN-DATE           PIC X(10)
 *   05 ACCT-EXPIRAION-DATE      PIC X(10)
 *   05 ACCT-REISSUE-DATE        PIC X(10)
 *   05 ACCT-CURR-CYC-CREDIT     PIC S9(10)V99
 *   05 ACCT-CURR-CYC-DEBIT      PIC S9(10)V99
 *   05 ACCT-ADDR-ZIP            PIC X(10)
 *   05 ACCT-GROUP-ID            PIC X(10)
 *   05 FILLER                   PIC X(178)
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "acct_id", length = 11)
    private String acctId;

    @Column(name = "acct_active_status", length = 1)
    private String acctActiveStatus;

    @Column(name = "acct_curr_bal", precision = 12, scale = 2)
    private BigDecimal acctCurrBal;

    @Column(name = "acct_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCreditLimit;

    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal acctCashCreditLimit;

    @Column(name = "acct_open_date", length = 10)
    private String acctOpenDate;

    @Column(name = "acct_expiration_date", length = 10)
    private String acctExpirationDate;

    @Column(name = "acct_reissue_date", length = 10)
    private String acctReissueDate;

    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycCredit;

    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal acctCurrCycDebit;

    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;

    public Account() {
    }

    public Account(String acctId, String acctActiveStatus, BigDecimal acctCurrBal,
                   BigDecimal acctCreditLimit, BigDecimal acctCashCreditLimit,
                   String acctOpenDate, String acctExpirationDate, String acctReissueDate,
                   BigDecimal acctCurrCycCredit, BigDecimal acctCurrCycDebit,
                   String acctAddrZip, String acctGroupId) {
        this.acctId = acctId;
        this.acctActiveStatus = acctActiveStatus;
        this.acctCurrBal = acctCurrBal;
        this.acctCreditLimit = acctCreditLimit;
        this.acctCashCreditLimit = acctCashCreditLimit;
        this.acctOpenDate = acctOpenDate;
        this.acctExpirationDate = acctExpirationDate;
        this.acctReissueDate = acctReissueDate;
        this.acctCurrCycCredit = acctCurrCycCredit;
        this.acctCurrCycDebit = acctCurrCycDebit;
        this.acctAddrZip = acctAddrZip;
        this.acctGroupId = acctGroupId;
    }

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }

    public String getAcctActiveStatus() {
        return acctActiveStatus;
    }

    public void setAcctActiveStatus(String acctActiveStatus) {
        this.acctActiveStatus = acctActiveStatus;
    }

    public BigDecimal getAcctCurrBal() {
        return acctCurrBal;
    }

    public void setAcctCurrBal(BigDecimal acctCurrBal) {
        this.acctCurrBal = acctCurrBal;
    }

    public BigDecimal getAcctCreditLimit() {
        return acctCreditLimit;
    }

    public void setAcctCreditLimit(BigDecimal acctCreditLimit) {
        this.acctCreditLimit = acctCreditLimit;
    }

    public BigDecimal getAcctCashCreditLimit() {
        return acctCashCreditLimit;
    }

    public void setAcctCashCreditLimit(BigDecimal acctCashCreditLimit) {
        this.acctCashCreditLimit = acctCashCreditLimit;
    }

    public String getAcctOpenDate() {
        return acctOpenDate;
    }

    public void setAcctOpenDate(String acctOpenDate) {
        this.acctOpenDate = acctOpenDate;
    }

    public String getAcctExpirationDate() {
        return acctExpirationDate;
    }

    public void setAcctExpirationDate(String acctExpirationDate) {
        this.acctExpirationDate = acctExpirationDate;
    }

    public String getAcctReissueDate() {
        return acctReissueDate;
    }

    public void setAcctReissueDate(String acctReissueDate) {
        this.acctReissueDate = acctReissueDate;
    }

    public BigDecimal getAcctCurrCycCredit() {
        return acctCurrCycCredit;
    }

    public void setAcctCurrCycCredit(BigDecimal acctCurrCycCredit) {
        this.acctCurrCycCredit = acctCurrCycCredit;
    }

    public BigDecimal getAcctCurrCycDebit() {
        return acctCurrCycDebit;
    }

    public void setAcctCurrCycDebit(BigDecimal acctCurrCycDebit) {
        this.acctCurrCycDebit = acctCurrCycDebit;
    }

    public String getAcctAddrZip() {
        return acctAddrZip;
    }

    public void setAcctAddrZip(String acctAddrZip) {
        this.acctAddrZip = acctAddrZip;
    }

    public String getAcctGroupId() {
        return acctGroupId;
    }

    public void setAcctGroupId(String acctGroupId) {
        this.acctGroupId = acctGroupId;
    }

    @Override
    public String toString() {
        return String.format(
                "Account{acctId='%s', status='%s', bal=%s, creditLimit=%s, group='%s'}",
                acctId, acctActiveStatus, acctCurrBal, acctCreditLimit, acctGroupId);
    }
}
