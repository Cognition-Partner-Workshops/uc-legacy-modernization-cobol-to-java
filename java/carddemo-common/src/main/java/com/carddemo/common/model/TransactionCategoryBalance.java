package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * JPA entity mapping for the Transaction Category Balance VSAM record.
 * Maps to COBOL copybook: CVTRA01Y.cpy (50-byte record)
 * Composite key: accountId + typeCode + categoryCode
 *
 * COBOL record layout:
 *   TRAN-CAT-KEY:
 *     TRANCAT-ACCT-ID   PIC 9(11)
 *     TRANCAT-TYPE-CD   PIC X(02)
 *     TRANCAT-CD        PIC 9(04)
 *   TRAN-CAT-BAL        PIC S9(09)V99
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_category_balances")
@IdClass(TransactionCategoryBalance.TransactionCategoryBalanceId.class)
public class TransactionCategoryBalance {

    @Id
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Id
    @Column(name = "category_code", length = 4, nullable = false)
    private String categoryCode;

    @Column(name = "balance", precision = 11, scale = 2)
    private BigDecimal balance;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionCategoryBalanceId implements Serializable {
        private Long accountId;
        private String typeCode;
        private String categoryCode;
    }
}
