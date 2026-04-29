package com.cardemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped from COBOL copybook CVTRA03Y.cpy (Transaction type, RECLN 60).
 * Source: app/cpy/CVTRA03Y.cpy
 *
 * TODO: Implement CRUD from COTRTUPC.cbl and COTRTLIC.cbl (DB2 extension)
 */
@Entity
@Table(name = "transaction_types")
public class TransactionType {

    @Id
    @Column(name = "tran_type", length = 2)
    private String tranType;

    @Column(name = "tran_type_desc", length = 50)
    private String tranTypeDesc;

    public TransactionType() {
    }

    public String getTranType() {
        return tranType;
    }

    public void setTranType(String tranType) {
        this.tranType = tranType;
    }

    public String getTranTypeDesc() {
        return tranTypeDesc;
    }

    public void setTranTypeDesc(String tranTypeDesc) {
        this.tranTypeDesc = tranTypeDesc;
    }
}
