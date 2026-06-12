package com.carddemo.refdata.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Maps to COBOL copybook CVTRA02Y.cpy — DIS-GROUP-RECORD (RECLN=50).
 *
 * <pre>
 *   05 DIS-GROUP-KEY
 *     10 DIS-ACCT-GROUP-ID  PIC X(10)     -> accountGroupId  CHAR(10), PK part 1
 *     10 DIS-TRAN-TYPE-CD   PIC X(02)     -> transactionTypeCode CHAR(2), PK part 2
 *     10 DIS-TRAN-CAT-CD    PIC 9(04)     -> transactionCategoryCode INT, PK part 3
 *   05 DIS-INT-RATE         PIC S9(04)V99 -> interestRate DECIMAL(6,2)
 *   05 FILLER               PIC X(28)     -- padding, not mapped
 * </pre>
 */
@Entity
@Table(name = "disclosure_group")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "account_group_id", length = 10, nullable = false)
    @NotBlank
    @Size(min = 1, max = 10)
    private String accountGroupId;

    @Id
    @Column(name = "transaction_type_code", length = 2, nullable = false)
    @NotBlank
    @Size(min = 1, max = 2)
    private String transactionTypeCode;

    @Id
    @Column(name = "transaction_category_code", nullable = false)
    @Min(0)
    @Max(9999)
    private int transactionCategoryCode;

    @Column(name = "interest_rate", precision = 6, scale = 2)
    @NotNull
    @Digits(integer = 4, fraction = 2)
    private BigDecimal interestRate;

    public DisclosureGroup() {
    }

    public DisclosureGroup(String accountGroupId, String transactionTypeCode,
                           int transactionCategoryCode, BigDecimal interestRate) {
        this.accountGroupId = accountGroupId;
        this.transactionTypeCode = transactionTypeCode;
        this.transactionCategoryCode = transactionCategoryCode;
        this.interestRate = interestRate;
    }

    public String getAccountGroupId() {
        return accountGroupId;
    }

    public void setAccountGroupId(String accountGroupId) {
        this.accountGroupId = accountGroupId;
    }

    public String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public void setTransactionTypeCode(String transactionTypeCode) {
        this.transactionTypeCode = transactionTypeCode;
    }

    public int getTransactionCategoryCode() {
        return transactionCategoryCode;
    }

    public void setTransactionCategoryCode(int transactionCategoryCode) {
        this.transactionCategoryCode = transactionCategoryCode;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }
}
