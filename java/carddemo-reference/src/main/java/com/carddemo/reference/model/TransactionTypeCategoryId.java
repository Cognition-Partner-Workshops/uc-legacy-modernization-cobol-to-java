package com.carddemo.reference.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link TransactionTypeCategory}.
 *
 * <p>Mirrors the DB2 primary key {@code (TRC_TYPE_CODE, TRC_TYPE_CATEGORY)} of
 * {@code CARDDEMO.TRANSACTION_TYPE_CATEGORY}
 * ({@code app/app-transaction-type-db2/ddl/TRNTYCAT.ddl}) and the
 * {@code TRAN-CAT-KEY} group in {@code app/cpy/CVTRA04Y.cpy}:</p>
 * <pre>
 * 05 TRAN-CAT-KEY.
 *    10 TRAN-TYPE-CD  PIC X(02).   -- TRC_TYPE_CODE     CHAR(2)
 *    10 TRAN-CAT-CD   PIC 9(04).   -- TRC_TYPE_CATEGORY CHAR(4)
 * </pre>
 */
@Embeddable
public class TransactionTypeCategoryId implements Serializable {

    private static final long serialVersionUID = 1L;

    /** TRC_TYPE_CODE CHAR(2) &ndash; FK to TRANSACTION_TYPE.TR_TYPE. */
    @Column(name = "TRC_TYPE_CODE", length = 2, nullable = false)
    private String typeCode;

    /** TRC_TYPE_CATEGORY CHAR(4) (e.g. "0001"). */
    @Column(name = "TRC_TYPE_CATEGORY", length = 4, nullable = false)
    private String categoryCode;

    public TransactionTypeCategoryId() {
    }

    public TransactionTypeCategoryId(String typeCode, String categoryCode) {
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransactionTypeCategoryId)) {
            return false;
        }
        TransactionTypeCategoryId that = (TransactionTypeCategoryId) o;
        return Objects.equals(typeCode, that.typeCode)
                && Objects.equals(categoryCode, that.categoryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeCode, categoryCode);
    }
}
