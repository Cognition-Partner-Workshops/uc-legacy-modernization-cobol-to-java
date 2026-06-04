package com.carddemo.reference.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Transaction category reference entity (child of {@link TransactionType}).
 *
 * <p>Maps to the DB2 table {@code CARDDEMO.TRANSACTION_TYPE_CATEGORY} defined
 * in {@code app/app-transaction-type-db2/ddl/TRNTYCAT.ddl} and the VSAM record
 * {@code TRAN-CAT-RECORD} in {@code app/cpy/CVTRA04Y.cpy}:</p>
 * <pre>
 * 01 TRAN-CAT-RECORD.
 *    05 TRAN-CAT-KEY.
 *       10 TRAN-TYPE-CD     PIC X(02).  -- TRC_TYPE_CODE     CHAR(2)
 *       10 TRAN-CAT-CD      PIC 9(04).  -- TRC_TYPE_CATEGORY CHAR(4)
 *    05 TRAN-CAT-TYPE-DESC  PIC X(50).  -- TRC_CAT_DATA      VARCHAR(50)
 *    05 FILLER              PIC X(04).
 * </pre>
 *
 * <p>The foreign key {@code TRC_TYPE_CODE -> TRANSACTION_TYPE.TR_TYPE} is
 * declared {@code ON DELETE RESTRICT}; a transaction type with categories
 * cannot be deleted (enforced in the service layer for portability across
 * databases).</p>
 */
@Entity
@Table(name = "TRANSACTION_TYPE_CATEGORY")
public class TransactionTypeCategory {

    @EmbeddedId
    private TransactionTypeCategoryId id;

    /** TRC_CAT_DATA VARCHAR(50). */
    @Column(name = "TRC_CAT_DATA", length = 50, nullable = false)
    private String description;

    public TransactionTypeCategory() {
    }

    public TransactionTypeCategory(TransactionTypeCategoryId id, String description) {
        this.id = id;
        this.description = description;
    }

    public TransactionTypeCategory(String typeCode, String categoryCode, String description) {
        this.id = new TransactionTypeCategoryId(typeCode, categoryCode);
        this.description = description;
    }

    public TransactionTypeCategoryId getId() {
        return id;
    }

    public void setId(TransactionTypeCategoryId id) {
        this.id = id;
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
        if (!(o instanceof TransactionTypeCategory)) {
            return false;
        }
        TransactionTypeCategory that = (TransactionTypeCategory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
