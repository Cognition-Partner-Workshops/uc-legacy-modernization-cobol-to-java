package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Transaction Type entity - migrated from COBOL copybook CVTRA03Y.cpy.
 * Original COBOL record length: 60 bytes.
 */
@Entity
@Table(name = "transaction_types")
public class TransactionTypeRecord {

    @Id
    @Column(name = "tran_type", length = 2)
    private String tranType;

    @Column(name = "tran_type_desc", length = 50)
    private String tranTypeDesc;

    public TransactionTypeRecord() {}

    public String getTranType() { return tranType; }
    public void setTranType(String tranType) { this.tranType = tranType; }
    public String getTranTypeDesc() { return tranTypeDesc; }
    public void setTranTypeDesc(String tranTypeDesc) { this.tranTypeDesc = tranTypeDesc; }

    @Override
    public String toString() {
        return "TransactionTypeRecord{tranType='" + tranType + "', tranTypeDesc='" + tranTypeDesc + "'}";
    }
}
