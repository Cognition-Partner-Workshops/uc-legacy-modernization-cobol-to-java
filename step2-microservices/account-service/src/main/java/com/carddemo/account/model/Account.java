package com.carddemo.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @Column(name = "acct_id")
    private Long acctId;

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

    @Column(name = "acct_expiraion_date", length = 10)
    private String acctExpiraionDate;

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

    public Account() {}

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
    public String getAcctActiveStatus() { return acctActiveStatus; }
    public void setAcctActiveStatus(String s) { this.acctActiveStatus = s; }
    public BigDecimal getAcctCurrBal() { return acctCurrBal; }
    public void setAcctCurrBal(BigDecimal v) { this.acctCurrBal = v; }
    public BigDecimal getAcctCreditLimit() { return acctCreditLimit; }
    public void setAcctCreditLimit(BigDecimal v) { this.acctCreditLimit = v; }
    public BigDecimal getAcctCashCreditLimit() { return acctCashCreditLimit; }
    public void setAcctCashCreditLimit(BigDecimal v) { this.acctCashCreditLimit = v; }
    public String getAcctOpenDate() { return acctOpenDate; }
    public void setAcctOpenDate(String s) { this.acctOpenDate = s; }
    public String getAcctExpiraionDate() { return acctExpiraionDate; }
    public void setAcctExpiraionDate(String s) { this.acctExpiraionDate = s; }
    public String getAcctReissueDate() { return acctReissueDate; }
    public void setAcctReissueDate(String s) { this.acctReissueDate = s; }
    public BigDecimal getAcctCurrCycCredit() { return acctCurrCycCredit; }
    public void setAcctCurrCycCredit(BigDecimal v) { this.acctCurrCycCredit = v; }
    public BigDecimal getAcctCurrCycDebit() { return acctCurrCycDebit; }
    public void setAcctCurrCycDebit(BigDecimal v) { this.acctCurrCycDebit = v; }
    public String getAcctAddrZip() { return acctAddrZip; }
    public void setAcctAddrZip(String s) { this.acctAddrZip = s; }
    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String s) { this.acctGroupId = s; }
}
