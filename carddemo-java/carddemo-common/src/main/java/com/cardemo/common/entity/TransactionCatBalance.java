package com.cardemo.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * JPA entity mapped from COBOL copybook CVTRA01Y.cpy (Transaction category balance, RECLN 50).
 * Source: app/cpy/CVTRA01Y.cpy
 *
 * TODO: Implement balance update logic from CBACT04C.cbl (Interest Calculation batch)
 */
@Entity
@Table(name = "tran_cat_balance")
@IdClass(TransactionCatBalanceId.class)
public class TransactionCatBalance {

    @Id
    @Column(name = "acct_id")
    private Long acctId;

    @Id
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    @Id
    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2, nullable = false)
    private BigDecimal tranCatBal = BigDecimal.ZERO;

    public TransactionCatBalance() {
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public BigDecimal getTranCatBal() {
        return tranCatBal;
    }

    public void setTranCatBal(BigDecimal tranCatBal) {
        this.tranCatBal = tranCatBal;
    }
}
