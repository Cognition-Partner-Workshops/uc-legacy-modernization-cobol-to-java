package com.carddemo.reference.repository;

import com.carddemo.reference.model.TransactionTypeCategory;
import com.carddemo.reference.model.TransactionTypeCategoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link TransactionTypeCategory}.
 *
 * <p>Backs the transaction category reference data described by
 * {@code app/cpy/CVTRA04Y.cpy} and DB2 table
 * {@code CARDDEMO.TRANSACTION_TYPE_CATEGORY}.</p>
 */
@Repository
public interface TransactionTypeCategoryRepository
        extends JpaRepository<TransactionTypeCategory, TransactionTypeCategoryId> {

    /** All categories belonging to a transaction type, ordered by category code. */
    List<TransactionTypeCategory> findByIdTypeCodeOrderByIdCategoryCode(String typeCode);

    /** Count of categories for a type &ndash; used to enforce ON DELETE RESTRICT. */
    long countByIdTypeCode(String typeCode);

    /** True if the type owns any categories (referential-integrity guard). */
    boolean existsByIdTypeCode(String typeCode);
}
