package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Transaction Type entity - mapped from COBOL copybook CVTRA03Y.cpy (TRAN-TYPE-RECORD).
 * Original VSAM dataset: AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS (RECLN 60).
 */
@Entity
@Table(name = "transaction_types")
public class TransactionType {

    @Id
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    @Column(name = "tran_type_desc", length = 50)
    private String tranTypeDesc;

    public TransactionType() {
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public String getTranTypeDesc() {
        return tranTypeDesc;
    }

    public void setTranTypeDesc(String tranTypeDesc) {
        this.tranTypeDesc = tranTypeDesc;
    }
}
