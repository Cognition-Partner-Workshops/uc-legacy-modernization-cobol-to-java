package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Transaction Category Type entity - migrated from COBOL copybook CVTRA04Y.cpy.
 * Original COBOL record length: 60 bytes.
 */
@Entity
@Table(name = "transaction_categories")
@IdClass(TransactionCategoryRecord.TranCatKey.class)
public class TransactionCategoryRecord {

    @Id
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    @Id
    @Column(name = "tran_cat_cd")
    private int tranCatCd;

    @Column(name = "tran_cat_type_desc", length = 50)
    private String tranCatTypeDesc;

    public TransactionCategoryRecord() {}

    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }
    public int getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(int tranCatCd) { this.tranCatCd = tranCatCd; }
    public String getTranCatTypeDesc() { return tranCatTypeDesc; }
    public void setTranCatTypeDesc(String tranCatTypeDesc) { this.tranCatTypeDesc = tranCatTypeDesc; }

    public static class TranCatKey implements Serializable {
        private String tranTypeCd;
        private int tranCatCd;

        public TranCatKey() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TranCatKey that = (TranCatKey) o;
            return tranCatCd == that.tranCatCd && Objects.equals(tranTypeCd, that.tranTypeCd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tranTypeCd, tranCatCd);
        }
    }
}
