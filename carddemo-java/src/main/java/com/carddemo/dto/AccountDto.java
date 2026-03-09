package com.carddemo.dto;

import java.math.BigDecimal;

/**
 * Account DTO for REST API responses.
 * Maps to the account view screen (BMS COACTVW / COBOL COACTVWC).
 */
public class AccountDto {

    private Long acctId;
    private String acctActiveStatus;
    private BigDecimal acctCurrBal;
    private BigDecimal acctCreditLimit;
    private BigDecimal acctCashCreditLimit;
    private String acctOpenDate;
    private String acctExpirationDate;
    private String acctReissueDate;
    private BigDecimal acctCurrCycCredit;
    private BigDecimal acctCurrCycDebit;
    private String acctAddrZip;
    private String acctGroupId;

    // Associated customer info (populated via card xref lookup)
    private Long custId;
    private String custFirstName;
    private String custLastName;

    public AccountDto() {
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

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public String getCustFirstName() {
        return custFirstName;
    }

    public void setCustFirstName(String custFirstName) {
        this.custFirstName = custFirstName;
    }

    public String getCustLastName() {
        return custLastName;
    }

    public void setCustLastName(String custLastName) {
        this.custLastName = custLastName;
    }
}
