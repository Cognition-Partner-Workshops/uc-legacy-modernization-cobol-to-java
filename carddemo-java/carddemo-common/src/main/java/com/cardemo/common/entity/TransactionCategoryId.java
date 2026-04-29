package com.cardemo.common.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for TransactionCategory entity.
 * Maps the composite key (TRAN-TYPE-CD + TRAN-CAT-CD) from CVTRA04Y.cpy.
 */
public class TransactionCategoryId implements Serializable {

    private String tranTypeCd;
    private Integer tranCatCd;

    public TransactionCategoryId() {
    }

    public TransactionCategoryId(String tranTypeCd, Integer tranCatCd) {
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionCategoryId that = (TransactionCategoryId) o;
        return Objects.equals(tranTypeCd, that.tranTypeCd) && Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tranTypeCd, tranCatCd);
    }
}
