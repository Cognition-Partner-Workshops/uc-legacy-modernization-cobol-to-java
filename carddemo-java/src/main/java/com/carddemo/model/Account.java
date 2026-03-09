package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Account entity - mapped from COBOL copybook CVACT01Y.cpy (ACCOUNT-RECORD).
 * Original VSAM dataset: AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS (RECLN 300).
 * Primary key: ACCT-ID PIC 9(11).
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "acct_id")
    private Long acctId;

    @Column(name = "acct_active_status", length = 1, nullable = false)
    private String acctActiveStatus = "Y";

    @Column(name = "acct_curr_bal", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCurrBal = BigDecimal.ZERO;

    @Column(name = "acct_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCreditLimit = BigDecimal.ZERO;

    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCashCreditLimit = BigDecimal.ZERO;

    @Column(name = "acct_open_date", length = 10)
    private String acctOpenDate;

    @Column(name = "acct_expiration_date", length = 10)
    private String acctExpirationDate;

    @Column(name = "acct_reissue_date", length = 10)
    private String acctReissueDate;

    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCurrCycCredit = BigDecimal.ZERO;

    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCurrCycDebit = BigDecimal.ZERO;

    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    @Column(name = "acct_group_id", length = 10)
    private String acctGroupId;

    public Account() {
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
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
}
