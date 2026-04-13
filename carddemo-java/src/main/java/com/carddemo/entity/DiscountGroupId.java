package com.carddemo.entity;

import java.io.Serializable;
import java.util.Objects;

public class DiscountGroupId implements Serializable {

    private String acctGroupId;
    private String tranTypeCd;
    private Integer tranCatCd;

    public DiscountGroupId() {}

    public DiscountGroupId(String acctGroupId, String tranTypeCd, Integer tranCatCd) {
        this.acctGroupId = acctGroupId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public String getAcctGroupId() { return acctGroupId; }
    public void setAcctGroupId(String acctGroupId) { this.acctGroupId = acctGroupId; }

    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }

    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer tranCatCd) { this.tranCatCd = tranCatCd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscountGroupId that = (DiscountGroupId) o;
        return Objects.equals(acctGroupId, that.acctGroupId) &&
               Objects.equals(tranTypeCd, that.tranTypeCd) &&
               Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctGroupId, tranTypeCd, tranCatCd);
    }
}
