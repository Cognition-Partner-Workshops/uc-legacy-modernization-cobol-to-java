package com.carddemo.batch.model;

/**
 * Java equivalent of COBOL copybook CVTRA03Y - Transaction Type Record (RECLN 60).
 *
 * Original COBOL layout:
 *   05 TRAN-TYPE       PIC X(02)
 *   05 TRAN-TYPE-DESC  PIC X(50)
 *   05 FILLER          PIC X(08)
 */
public class TransactionTypeRecord {

    private String type;
    private String description;

    public TransactionTypeRecord() {}

    public TransactionTypeRecord(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "TransactionTypeRecord{type='" + type + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}
