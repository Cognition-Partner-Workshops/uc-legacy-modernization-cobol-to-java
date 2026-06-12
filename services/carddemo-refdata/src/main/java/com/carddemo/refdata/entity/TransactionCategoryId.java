package com.carddemo.refdata.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for TransactionCategory, matching the COBOL
 * TRAN-CAT-KEY group in CVTRA04Y.cpy (TRAN-TYPE-CD + TRAN-CAT-CD).
 */
public class TransactionCategoryId implements Serializable {

    private String typeCode;
    private int categoryCode;

    public TransactionCategoryId() {
    }

    public TransactionCategoryId(String typeCode, int categoryCode) {
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategoryId that = (TransactionCategoryId) o;
        return categoryCode == that.categoryCode && Objects.equals(typeCode, that.typeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeCode, categoryCode);
    }
}
