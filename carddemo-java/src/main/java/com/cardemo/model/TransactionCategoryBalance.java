package com.cardemo.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVTRA01Y (TRAN-CAT-BAL-RECORD).
 * Represents transaction category balance (record length 50).
 */
public class TransactionCategoryBalance {

    private long accountId;          // TRANCAT-ACCT-ID PIC 9(11)
    private String typeCode;         // TRANCAT-TYPE-CD PIC X(02)
    private int categoryCode;        // TRANCAT-CD PIC 9(04)
    private BigDecimal balance;      // TRAN-CAT-BAL PIC S9(09)V99

    public TransactionCategoryBalance() {
        this.balance = BigDecimal.ZERO;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
