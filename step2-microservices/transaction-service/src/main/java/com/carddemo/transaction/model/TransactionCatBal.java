package com.carddemo.transaction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "tran_cat_bal")
@IdClass(TransactionCatBal.TransactionCatBalId.class)
public class TransactionCatBal {

    @Id
    @Column(name = "trancat_acct_id")
    private Long trancatAcctId;

    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String trancatTypeCd;

    @Id
    @Column(name = "trancat_cd")
    private Integer trancatCd;

    @Column(name = "tran_cat_bal", precision = 12, scale = 2)
    private BigDecimal tranCatBal;

    public TransactionCatBal() {}

    public Long getTrancatAcctId() { return trancatAcctId; }
    public void setTrancatAcctId(Long v) { this.trancatAcctId = v; }
    public String getTrancatTypeCd() { return trancatTypeCd; }
    public void setTrancatTypeCd(String v) { this.trancatTypeCd = v; }
    public Integer getTrancatCd() { return trancatCd; }
    public void setTrancatCd(Integer v) { this.trancatCd = v; }
    public BigDecimal getTranCatBal() { return tranCatBal; }
    public void setTranCatBal(BigDecimal v) { this.tranCatBal = v; }

    public static class TransactionCatBalId implements Serializable {
        private Long trancatAcctId;
        private String trancatTypeCd;
        private Integer trancatCd;

        public TransactionCatBalId() {}
        public TransactionCatBalId(Long acctId, String typeCd, Integer cd) {
            this.trancatAcctId = acctId;
            this.trancatTypeCd = typeCd;
            this.trancatCd = cd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionCatBalId that = (TransactionCatBalId) o;
            return Objects.equals(trancatAcctId, that.trancatAcctId) &&
                   Objects.equals(trancatTypeCd, that.trancatTypeCd) &&
                   Objects.equals(trancatCd, that.trancatCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(trancatAcctId, trancatTypeCd, trancatCd);
        }
    }
}
