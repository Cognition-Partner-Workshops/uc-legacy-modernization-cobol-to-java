package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Transaction Category Balance entity - migrated from COBOL copybook CVTRA01Y.cpy.
 * Original COBOL record length: 50 bytes.
 */
@Entity
@Table(name = "tran_cat_bal")
@IdClass(TransactionCategoryBalanceRecord.TranCatBalKey.class)
public class TransactionCategoryBalanceRecord {

    @Id
    @Column(name = "trancat_acct_id")
    private long trancatAcctId;

    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String trancatTypeCd;

    @Id
    @Column(name = "trancat_cd")
    private int trancatCd;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal tranCatBal;

    public TransactionCategoryBalanceRecord() {
    }

    public long getTrancatAcctId() { return trancatAcctId; }
    public void setTrancatAcctId(long trancatAcctId) { this.trancatAcctId = trancatAcctId; }
    public String getTrancatTypeCd() { return trancatTypeCd; }
    public void setTrancatTypeCd(String trancatTypeCd) { this.trancatTypeCd = trancatTypeCd; }
    public int getTrancatCd() { return trancatCd; }
    public void setTrancatCd(int trancatCd) { this.trancatCd = trancatCd; }
    public BigDecimal getTranCatBal() { return tranCatBal; }
    public void setTranCatBal(BigDecimal tranCatBal) { this.tranCatBal = tranCatBal; }

    public static class TranCatBalKey implements Serializable {
        private long trancatAcctId;
        private String trancatTypeCd;
        private int trancatCd;

        public TranCatBalKey() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TranCatBalKey that = (TranCatBalKey) o;
            return trancatAcctId == that.trancatAcctId &&
                    trancatCd == that.trancatCd &&
                    Objects.equals(trancatTypeCd, that.trancatTypeCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(trancatAcctId, trancatTypeCd, trancatCd);
        }
    }
}
