package com.carddemo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tran_cat_bal")
public class TranCatBal {
    @EmbeddedId
    private TranCatBalId id;
    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal tranCatBal;

    protected TranCatBal() {
    }

    public TranCatBalId getId() { return id; }
    public void setId(TranCatBalId id) { this.id = id; }
    public BigDecimal getTranCatBal() { return tranCatBal; }
    public void setTranCatBal(BigDecimal tranCatBal) { this.tranCatBal = tranCatBal; }
}
