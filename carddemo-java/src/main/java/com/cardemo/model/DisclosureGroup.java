package com.cardemo.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVTRA02Y (DIS-GROUP-RECORD).
 * Represents disclosure group record (record length 50).
 */
public class DisclosureGroup {

    private String accountGroupId;    // DIS-ACCT-GROUP-ID PIC X(10)
    private String transactionTypeCode;  // DIS-TRAN-TYPE-CD PIC X(02)
    private int transactionCategoryCode; // DIS-TRAN-CAT-CD PIC 9(04)
    private BigDecimal interestRate;  // DIS-INT-RATE PIC S9(04)V99

    public DisclosureGroup() {
        this.interestRate = BigDecimal.ZERO;
    }

    public String getAccountGroupId() {
        return accountGroupId;
    }

    public void setAccountGroupId(String accountGroupId) {
        this.accountGroupId = accountGroupId;
    }

    public String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public void setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
    }

    public int getTransactionCategoryCode() {
        return transactionCategoryCode;
    }

    public void setTransactionCategoryCode(int transactionCategoryCode) {
        this.transactionCategoryCode = transactionCategoryCode;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }
}
