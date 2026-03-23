package com.carddemo.model;

/**
 * Transaction Category entity - modernized from COBOL copybook CVTRA04Y (TRAN-CAT-RECORD).
 *
 * Legacy COBOL layout (RECLN 60):
 *   TRAN-TYPE-CD          PIC X(02)   -> typeCode
 *   TRAN-CAT-CD           PIC 9(04)   -> categoryCode
 *   TRAN-CAT-TYPE-DESC    PIC X(50)   -> categoryDescription
 *
 * Stored in relational DB via JDBC (mirrors legacy DB2 table).
 */
public class TransactionCategory {

    private String typeCode;
    private Integer categoryCode;
    private String categoryDescription;

    public TransactionCategory() {}

    public TransactionCategory(String typeCode, Integer categoryCode, String categoryDescription) {
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
        this.categoryDescription = categoryDescription;
    }

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public Integer getCategoryCode() { return categoryCode; }
    public void setCategoryCode(Integer categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryDescription() { return categoryDescription; }
    public void setCategoryDescription(String categoryDescription) { this.categoryDescription = categoryDescription; }
}
