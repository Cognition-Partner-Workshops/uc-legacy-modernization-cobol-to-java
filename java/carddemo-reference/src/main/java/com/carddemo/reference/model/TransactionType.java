package com.carddemo.reference.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Transaction type reference entity.
 *
 * <p>Maps to the DB2 table {@code CARDDEMO.TRANSACTION_TYPE} defined in
 * {@code app/app-transaction-type-db2/ddl/TRNTYPE.ddl} and the VSAM record
 * {@code TRAN-TYPE-RECORD} in {@code app/cpy/CVTRA03Y.cpy}:</p>
 * <pre>
 * 01 TRAN-TYPE-RECORD.
 *    05 TRAN-TYPE        PIC X(02).   -- TR_TYPE        CHAR(2)     PK
 *    05 TRAN-TYPE-DESC   PIC X(50).   -- TR_DESCRIPTION VARCHAR(50)
 *    05 FILLER           PIC X(08).
 * </pre>
 *
 * <p>Maintained online by {@code COTRTLIC}/{@code COTRTUPC} and in batch by
 * {@code COBTUPDT}.</p>
 */
@Entity
@Table(name = "TRANSACTION_TYPE")
public class TransactionType {

    /** TR_TYPE CHAR(2) primary key (e.g. "01"). */
    @Id
    @Column(name = "TR_TYPE", length = 2, nullable = false)
    private String typeCode;

    /** TR_DESCRIPTION VARCHAR(50). */
    @Column(name = "TR_DESCRIPTION", length = 50, nullable = false)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionType)) {
            return false;
        }
        TransactionType that = (TransactionType) o;
        return Objects.equals(typeCode, that.typeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeCode);
    }
}
