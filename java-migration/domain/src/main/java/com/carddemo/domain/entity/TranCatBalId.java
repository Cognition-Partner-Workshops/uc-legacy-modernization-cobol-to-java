package com.carddemo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class TranCatBalId implements Serializable {
    @Column(name = "acct_id", precision = 11, scale = 0)
    private BigDecimal acctId;
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;
    @Column(name = "tran_cat_cd", precision = 4, scale = 0)
    private BigDecimal tranCatCd;

    protected TranCatBalId() {
    }

    public TranCatBalId(BigDecimal acctId, String tranTypeCd, BigDecimal tranCatCd) {
        this.acctId = acctId;
        this.tranTypeCd = tranTypeCd;
        this.tranCatCd = tranCatCd;
    }

    public BigDecimal getAcctId() { return acctId; }
    public void setAcctId(BigDecimal acctId) { this.acctId = acctId; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }
    public BigDecimal getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(BigDecimal tranCatCd) { this.tranCatCd = tranCatCd; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranCatBalId that)) return false;
        return Objects.equals(acctId, that.acctId)
                && Objects.equals(tranTypeCd, that.tranTypeCd)
                && Objects.equals(tranCatCd, that.tranCatCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acctId, tranTypeCd, tranCatCd);
    }
}
