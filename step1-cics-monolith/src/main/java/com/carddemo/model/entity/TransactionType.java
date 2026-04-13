package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Mirrors CVTRA03Y copybook (TRAN-TYPE-RECORD).
 */
@Entity
@Table(name = "transaction_type")
public class TransactionType {

    @Id
    @Column(name = "tran_type", length = 2)
    private String tranType;

    @Column(name = "tran_type_desc", length = 50)
    private String tranTypeDesc;

    public TransactionType() {}

    public String getTranType() { return tranType; }
    public void setTranType(String tranType) { this.tranType = tranType; }

    public String getTranTypeDesc() { return tranTypeDesc; }
    public void setTranTypeDesc(String tranTypeDesc) { this.tranTypeDesc = tranTypeDesc; }
}
