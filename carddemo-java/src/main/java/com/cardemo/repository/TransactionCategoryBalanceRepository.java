package com.cardemo.repository;

import com.cardemo.model.TransactionCategoryBalance;
import java.util.Optional;

/**
 * Repository interface for transaction category balance data access.
 * Equivalent of COBOL TCATBAL VSAM file operations.
 */
public interface TransactionCategoryBalanceRepository {

    Optional<TransactionCategoryBalance> findByKey(long accountId, String typeCode,
                                                    int categoryCode);

    void save(TransactionCategoryBalance balance);

    void update(TransactionCategoryBalance balance);
}
