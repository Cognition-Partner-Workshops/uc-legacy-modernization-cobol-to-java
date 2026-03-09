package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Transaction Category entity - mapped from COBOL copybook CVTRA04Y.cpy (TRAN-CAT-RECORD).
 * Original VSAM dataset: AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS (RECLN 60).
 */
@Entity
@Table(name = "transaction_categories")
public class TransactionCategory {

    @Id
    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    @Column(name = "tran_cat_desc", length = 50)
    private String tranCatDesc;

    public TransactionCategory() {
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public String getTranCatDesc() {
        return tranCatDesc;
    }

    public void setTranCatDesc(String tranCatDesc) {
        this.tranCatDesc = tranCatDesc;
    }
}
