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

/**
 * JPA entity mapping for the Transaction Category VSAM record.
 * Maps to COBOL copybook: CVTRA04Y.cpy (60-byte record)
 * Composite key: typeCode + categoryCode
 *
 * COBOL record layout:
 *   TRAN-CAT-KEY:
 *     TRAN-TYPE-CD        PIC X(02)
 *     TRAN-CAT-CD         PIC 9(04)
 *   TRAN-CAT-TYPE-DESC    PIC X(50)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_categories")
@IdClass(TransactionCategory.TransactionCategoryId.class)
public class TransactionCategory {

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    private String typeCode;

    @Id
    @Column(name = "category_code", length = 4, nullable = false)
    private String categoryCode;

    @Column(name = "category_description", length = 50)
    private String categoryDescription;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionCategoryId implements Serializable {
        private String typeCode;
        private String categoryCode;
    }
}
