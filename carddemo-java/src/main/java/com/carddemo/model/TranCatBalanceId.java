package com.carddemo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for TranCatBalance entity.
 * Maps the VSAM composite key: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD.
 */
public class TranCatBalanceId implements Serializable {

    private Long trancatAcctId;
    private String trancatTypeCd;
    private Integer trancatCd;

    public TranCatBalanceId() {
    }

    public TranCatBalanceId(Long trancatAcctId, String trancatTypeCd, Integer trancatCd) {
        this.trancatAcctId = trancatAcctId;
        this.trancatTypeCd = trancatTypeCd;
        this.trancatCd = trancatCd;
    }

    public Long getTrancatAcctId() {
        return trancatAcctId;
    }

    public void setTrancatAcctId(Long trancatAcctId) {
        this.trancatAcctId = trancatAcctId;
    }

    public String getTrancatTypeCd() {
        return trancatTypeCd;
    }

    public void setTrancatTypeCd(String trancatTypeCd) {
        this.trancatTypeCd = trancatTypeCd;
    }

    public Integer getTrancatCd() {
        return trancatCd;
    }

    public void setTrancatCd(Integer trancatCd) {
        this.trancatCd = trancatCd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranCatBalanceId that = (TranCatBalanceId) o;
        return Objects.equals(trancatAcctId, that.trancatAcctId)
                && Objects.equals(trancatTypeCd, that.trancatTypeCd)
                && Objects.equals(trancatCd, that.trancatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trancatAcctId, trancatTypeCd, trancatCd);
    }
}
