package com.cardemo.common.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for TransactionCatBalance entity.
 * Maps the composite key (ACCT-ID + TYPE-CD + CAT-CD) from CVTRA01Y.cpy.
 */
public class TransactionCatBalanceId implements Serializable {

    private Long acctId;
    private String tranTypeCd;
    private Integer tranCatCd;

    public TransactionCatBalanceId() {
    }

    public TransactionCatBalanceId(Long acctId, String tranTypeCd, Integer tranCatCd) {
        this.acctId = acctId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
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
        TransactionCatBalanceId that = (TransactionCatBalanceId) o;
        return Objects.equals(acctId, that.acctId)
                && Objects.equals(tranTypeCd, that.tranTypeCd)
                && Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, tranTypeCd, tranCatCd);
    }
}
