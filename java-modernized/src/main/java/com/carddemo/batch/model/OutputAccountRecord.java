package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Mirrors OUT-ACCT-REC from CBACT01C.cbl (FD OUT-FILE).
 *
 * Notable difference from input: OUT-ACCT-CURR-CYC-DEBIT uses COMP-3
 * (packed decimal) in COBOL; in Java we keep it as BigDecimal but track
 * the distinction for serialisation purposes.
 *
 * The reissue date is stored in YYYYMMDD format (converted from YYYY-MM-DD
 * via COBDATFT / DateConverter).
 */
public class OutputAccountRecord {

    private long acctId;
    private char activeStatus;
    private BigDecimal currentBalance;
    private BigDecimal creditLimit;
    private BigDecimal cashCreditLimit;
    private String openDate;
    private String expirationDate;
    private String reissueDate;
    private BigDecimal currentCycleCredit;
    private BigDecimal currentCycleDebit;
    private String groupId;

    public OutputAccountRecord() {}

    public String toDelimited(String delimiter) {
        return String.join(delimiter,
                String.valueOf(acctId),
                String.valueOf(activeStatus),
                currentBalance.toPlainString(),
                creditLimit.toPlainString(),
                cashCreditLimit.toPlainString(),
                openDate,
                expirationDate,
                reissueDate,
                currentCycleCredit.toPlainString(),
                currentCycleDebit.toPlainString(),
                groupId);
    }

    // Getters and setters

    public long getAcctId() { return acctId; }
    public void setAcctId(long acctId) { this.acctId = acctId; }

    public char getActiveStatus() { return activeStatus; }
    public void setActiveStatus(char activeStatus) { this.activeStatus = activeStatus; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

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

    public BigDecimal getCurrentCycleCredit() { return currentCycleCredit; }
    public void setCurrentCycleCredit(BigDecimal currentCycleCredit) { this.currentCycleCredit = currentCycleCredit; }

    public BigDecimal getCurrentCycleDebit() { return currentCycleDebit; }
    public void setCurrentCycleDebit(BigDecimal currentCycleDebit) { this.currentCycleDebit = currentCycleDebit; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    @Override
    public String toString() {
        return "OutputAccountRecord{" +
                "acctId=" + acctId +
                ", activeStatus=" + activeStatus +
                ", currentBalance=" + currentBalance +
                ", creditLimit=" + creditLimit +
                ", cashCreditLimit=" + cashCreditLimit +
                ", openDate='" + openDate + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", reissueDate='" + reissueDate + '\'' +
                ", currentCycleCredit=" + currentCycleCredit +
                ", currentCycleDebit=" + currentCycleDebit +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
