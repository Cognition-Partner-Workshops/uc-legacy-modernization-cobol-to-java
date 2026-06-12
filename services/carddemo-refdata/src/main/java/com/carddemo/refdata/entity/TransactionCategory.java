package com.carddemo.refdata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Maps to COBOL copybook CVTRA04Y.cpy — TRAN-CAT-RECORD (RECLN=60).
 *
 * <pre>
 *   05 TRAN-CAT-KEY
 *     10 TRAN-TYPE-CD    PIC X(02)   -> typeCode   CHAR(2), composite PK part 1
 *     10 TRAN-CAT-CD     PIC 9(04)   -> categoryCode INT, composite PK part 2
 *   05 TRAN-CAT-TYPE-DESC PIC X(50)  -> description VARCHAR(50)
 *   05 FILLER             PIC X(04)  -- padding, not mapped
 * </pre>
 */
@Entity
@Table(name = "transaction_category")
@IdClass(TransactionCategoryId.class)
public class TransactionCategory {

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    @NotBlank
    @Size(min = 1, max = 2)
    private String typeCode;

    @Id
    @Column(name = "category_code", nullable = false)
    @Min(0)
    @Max(9999)
    private int categoryCode;

    @Column(name = "description", length = 50)
    @Size(max = 50)
    private String description;

    public TransactionCategory() {
    }

    public TransactionCategory(String typeCode, int categoryCode, String description) {
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
        this.description = description;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
