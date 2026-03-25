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
 * JPA entity mapping for the Disclosure Group VSAM record.
 * Maps to COBOL copybook: CVTRA02Y.cpy (50-byte record)
 * Composite key: accountGroupId + transactionTypeCode + transactionCategoryCode
 *
 * COBOL record layout:
 *   DIS-GROUP-KEY:
 *     DIS-ACCT-GROUP-ID   PIC X(10)
 *     DIS-TRAN-TYPE-CD    PIC X(02)
 *     DIS-TRAN-CAT-CD     PIC 9(04)
 *   DIS-INT-RATE           PIC S9(04)V99
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "disclosure_groups")
@IdClass(DisclosureGroup.DisclosureGroupId.class)
public class DisclosureGroup {

    @Id
    @Column(name = "account_group_id", length = 10, nullable = false)
    private String accountGroupId;

    @Id
    @Column(name = "transaction_type_code", length = 2, nullable = false)
    private String transactionTypeCode;

    @Id
    @Column(name = "transaction_category_code", length = 4, nullable = false)
    private String transactionCategoryCode;

    @Column(name = "interest_rate", precision = 6, scale = 2)
    private BigDecimal interestRate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisclosureGroupId implements Serializable {
        private String accountGroupId;
        private String transactionTypeCode;
        private String transactionCategoryCode;
    }
}
