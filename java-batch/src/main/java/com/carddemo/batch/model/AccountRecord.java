package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVACT01Y - Account Record (RECLN 300).
 *
 * Original COBOL layout:
 *   05 ACCT-ID                PIC 9(11)
 *   05 ACCT-ACTIVE-STATUS     PIC X(01)
 *   05 ACCT-CURR-BAL          PIC S9(10)V99
 *   05 ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *   05 ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *   05 ACCT-OPEN-DATE         PIC X(10)
 *   05 ACCT-EXPIRAION-DATE    PIC X(10)
 *   05 ACCT-REISSUE-DATE      PIC X(10)
 *   05 ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 *   05 ACCT-CURR-CYC-DEBIT    PIC S9(10)V99
 *   05 ACCT-ADDR-ZIP          PIC X(10)
 *   05 ACCT-GROUP-ID          PIC X(10)
 *   05 FILLER                 PIC X(178)
 */
public class AccountRecord {

    private long acctId;
    private String activeStatus;
    private BigDecimal currBal;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private String openDate;
    private String expirationDate;
    private String reissueDate;
    private BigDecimal currCycCredit;
    private BigDecimal currCycDebit;
    private String addrZip;
    private String groupId;

    public AccountRecord() {
        this.currBal = BigDecimal.ZERO;
        this.creditLimit = BigDecimal.ZERO;
        this.cashCreditLimit = BigDecimal.ZERO;
        this.currCycCredit = BigDecimal.ZERO;
        this.currCycDebit = BigDecimal.ZERO;
    }

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }

    public BigDecimal getCurrBal() { return currBal; }
    public void setCurrBal(BigDecimal currBal) { this.currBal = currBal; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public void setCashCreditLimit(BigDecimal cashCreditLimit) { this.cashCreditLimit = cashCreditLimit; }

    public String getOpenDate() { return openDate; }
    public void setOpenDate(String openDate) { this.openDate = openDate; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getReissueDate() { return reissueDate; }
    public void setReissueDate(String reissueDate) { this.reissueDate = reissueDate; }

    public BigDecimal getCurrCycCredit() { return currCycCredit; }
    public void setCurrCycCredit(BigDecimal currCycCredit) { this.currCycCredit = currCycCredit; }

    public BigDecimal getCurrCycDebit() { return currCycDebit; }
    public void setCurrCycDebit(BigDecimal currCycDebit) { this.currCycDebit = currCycDebit; }

    public String getAddrZip() { return addrZip; }
    public void setAddrZip(String addrZip) { this.addrZip = addrZip; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    @Override
    public String toString() {
        return "AccountRecord{acctId=" + acctId +
               ", activeStatus='" + activeStatus + '\'' +
               ", currBal=" + currBal +
               ", creditLimit=" + creditLimit +
               ", expirationDate='" + expirationDate + '\'' +
               ", groupId='" + groupId + '\'' +
               '}';
    }
}
