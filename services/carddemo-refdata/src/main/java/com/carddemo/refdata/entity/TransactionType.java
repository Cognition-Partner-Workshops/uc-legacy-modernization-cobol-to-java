package com.carddemo.refdata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Maps to COBOL copybook CVTRA03Y.cpy — TRAN-TYPE-RECORD (RECLN=60).
 *
 * <pre>
 *   05 TRAN-TYPE      PIC X(02)   -> typeCode  CHAR(2), PK
 *   05 TRAN-TYPE-DESC PIC X(50)   -> description VARCHAR(50)
 *   05 FILLER         PIC X(08)   -- padding, not mapped
 * </pre>
 */
@Entity
@Table(name = "transaction_type")
public class TransactionType {

    @Id
    @Column(name = "type_code", length = 2, nullable = false)
    @NotBlank
    @Size(min = 1, max = 2)
    private String typeCode;

    @Column(name = "description", length = 50)
    @Size(max = 50)
    private String description;

    public TransactionType() {
    }

    public TransactionType(String typeCode, String description) {
        this.typeCode = typeCode;
        this.description = description;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
