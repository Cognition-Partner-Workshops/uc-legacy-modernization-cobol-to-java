package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction category balance entity - replaces TCATBAL VSAM file (CVTRA01Y copybook).
 * Maintains running balances per account/type/category.
 * Original COBOL record: 50 bytes with composite key (ACCT-ID + TYPE-CD + CAT-CD).
 */
@Entity
@Table(name = "tran_cat_balances")
@IdClass(TranCatBalanceId.class)
public class TranCatBalance {

    @Id
    @Column(name = "account_id")
    private Long accountId;

    @Id
    @Column(name = "type_code", length = 2)
    private String typeCode;

    @Id
    @Column(name = "category_code")
    private Integer categoryCode;

    @Column(name = "balance", precision = 11, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TranCatBalance() {}

    public TranCatBalance(Long accountId, String typeCode, Integer categoryCode, BigDecimal balance) {
        this.accountId = accountId;
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
        this.balance = balance;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public Integer getCategoryCode() { return categoryCode; }
    public void setCategoryCode(Integer categoryCode) { this.categoryCode = categoryCode; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
