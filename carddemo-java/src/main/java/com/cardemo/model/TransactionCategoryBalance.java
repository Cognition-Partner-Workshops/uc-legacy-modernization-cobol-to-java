package com.cardemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Transaction Category Balance entity - converted from COBOL copybook CVTRA01Y.cpy
 * Original VSAM record length: 50 bytes
 */
@Entity
@Table(name = "transaction_category_balances")
@IdClass(TransactionCategoryBalanceKey.class)
public class TransactionCategoryBalance {

    @Id
    @Column(name = "trancat_acct_id")
    private Long acctId;

    @Id
    @Column(name = "trancat_type_cd", length = 2)
    private String typeCd;

    @Id
    @Column(name = "trancat_cd")
    private Integer catCd;

    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal balance;

    public TransactionCategoryBalance() {}

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }

    public String getTypeCd() { return typeCd; }
    public void setTypeCd(String typeCd) { this.typeCd = typeCd; }

    public Integer getCatCd() { return catCd; }
    public void setCatCd(Integer catCd) { this.catCd = catCd; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
