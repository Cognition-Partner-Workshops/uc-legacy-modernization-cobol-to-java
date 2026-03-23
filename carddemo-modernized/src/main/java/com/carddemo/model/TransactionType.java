package com.carddemo.model;

/**
 * Transaction Type entity - modernized from COBOL copybook CVTRA03Y (TRAN-TYPE-RECORD).
 *
 * Legacy COBOL layout (RECLN 60):
 *   TRAN-TYPE       PIC X(02)   -> typeCode
 *   TRAN-TYPE-DESC  PIC X(50)   -> typeDescription
 *
 * Stored in relational DB via JDBC (mirrors legacy DB2 table).
 */
public class TransactionType {

    private String typeCode;
    private String typeDescription;

    public TransactionType() {}

    public TransactionType(String typeCode, String typeDescription) {
        this.typeCode = typeCode;
        this.typeDescription = typeDescription;
    }

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
    public String getTypeDescription() { return typeDescription; }
    public void setTypeDescription(String typeDescription) { this.typeDescription = typeDescription; }
}
