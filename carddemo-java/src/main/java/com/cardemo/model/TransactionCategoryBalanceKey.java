package com.cardemo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for TransactionCategoryBalance entity.
 */
public class TransactionCategoryBalanceKey implements Serializable {

    private Long acctId;
    private String typeCd;
    private Integer catCd;

    public TransactionCategoryBalanceKey() {}

    public TransactionCategoryBalanceKey(Long acctId, String typeCd, Integer catCd) {
        this.acctId = acctId;
        this.typeCd = typeCd;
        this.catCd = catCd;
    }

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategoryBalanceKey that = (TransactionCategoryBalanceKey) o;
        return Objects.equals(acctId, that.acctId)
                && Objects.equals(typeCd, that.typeCd)
                && Objects.equals(catCd, that.catCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, typeCd, catCd);
    }
}
