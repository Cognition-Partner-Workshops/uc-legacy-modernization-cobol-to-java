package com.carddemo.entity;

import java.io.Serializable;
import java.util.Objects;

public class TranCatBalanceId implements Serializable {

    private Long acctId;
    private String typeCd;
    private Integer catCd;

    public TranCatBalanceId() {}

    public TranCatBalanceId(Long acctId, String typeCd, Integer catCd) {
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
        TranCatBalanceId that = (TranCatBalanceId) o;
        return Objects.equals(acctId, that.acctId) &&
               Objects.equals(typeCd, that.typeCd) &&
               Objects.equals(catCd, that.catCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, typeCd, catCd);
    }
}
