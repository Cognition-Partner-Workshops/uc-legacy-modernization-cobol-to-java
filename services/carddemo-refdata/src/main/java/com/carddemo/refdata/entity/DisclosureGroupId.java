package com.carddemo.refdata.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for DisclosureGroup, matching the COBOL
 * DIS-GROUP-KEY in CVTRA02Y.cpy (DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD).
 */
public class DisclosureGroupId implements Serializable {

    private String accountGroupId;
    private String transactionTypeCode;
    private int transactionCategoryCode;

    public DisclosureGroupId() {
    }

    public DisclosureGroupId(String accountGroupId, String transactionTypeCode, int transactionCategoryCode) {
        this.accountGroupId = accountGroupId;
        this.transactionTypeCode = transactionTypeCode;
        this.transactionCategoryCode = transactionCategoryCode;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisclosureGroupId that = (DisclosureGroupId) o;
        return transactionCategoryCode == that.transactionCategoryCode
                && Objects.equals(accountGroupId, that.accountGroupId)
                && Objects.equals(transactionTypeCode, that.transactionTypeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountGroupId, transactionTypeCode, transactionCategoryCode);
    }
}
