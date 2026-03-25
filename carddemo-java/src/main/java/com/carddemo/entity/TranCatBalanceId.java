package com.carddemo.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for TranCatBalance entity.
 */
public class TranCatBalanceId implements Serializable {

    private Long accountId;
    private String typeCode;
    private Integer categoryCode;

    public TranCatBalanceId() {}

    public TranCatBalanceId(Long accountId, String typeCode, Integer categoryCode) {
        this.accountId = accountId;
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranCatBalanceId that = (TranCatBalanceId) o;
        return Objects.equals(accountId, that.accountId)
                && Objects.equals(typeCode, that.typeCode)
                && Objects.equals(categoryCode, that.categoryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, typeCode, categoryCode);
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public Integer getCategoryCode() { return categoryCode; }
    public void setCategoryCode(Integer categoryCode) { this.categoryCode = categoryCode; }
}
