package com.carddemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Transaction Category Balance entity - mapped from COBOL copybook CVTRA01Y.cpy (TRAN-CAT-BAL-RECORD).
 * Original VSAM dataset: AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS (RECLN 50).
 * Composite key: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD.
 */
@Entity
@Table(name = "tran_cat_balances")
@IdClass(TranCatBalanceId.class)
public class TranCatBalance {

    @Id
    @Column(name = "trancat_acct_id")
    private Long trancatAcctId;

    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String trancatTypeCd;

    @Id
    @Column(name = "trancat_cd")
    private Integer trancatCd;

    @Column(name = "trancat_bal", precision = 12, scale = 2, nullable = false)
    private BigDecimal trancatBal = BigDecimal.ZERO;

    public TranCatBalance() {
    }

    public Long getTrancatAcctId() {
        return trancatAcctId;
    }

    public void setTrancatAcctId(Long trancatAcctId) {
        this.trancatAcctId = trancatAcctId;
    }

    public String getTrancatTypeCd() {
        return trancatTypeCd;
    }

    public void setTrancatTypeCd(String trancatTypeCd) {
        this.trancatTypeCd = trancatTypeCd;
    }

    public Integer getTrancatCd() {
        return trancatCd;
    }

    public void setTrancatCd(Integer trancatCd) {
        this.trancatCd = trancatCd;
    }

    public BigDecimal getTrancatBal() {
        return trancatBal;
    }

    public void setTrancatBal(BigDecimal trancatBal) {
        this.trancatBal = trancatBal;
    }
}
