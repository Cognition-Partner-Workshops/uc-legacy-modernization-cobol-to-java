package com.cardemo.repository;

import com.cardemo.model.TransactionCategoryBalance;
import com.cardemo.model.TransactionCategoryBalanceKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TransactionCategoryBalance entity - replaces VSAM KSDS file access
 * (TCATBALF in COBOL programs)
 */
@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceKey> {

    List<TransactionCategoryBalance> findByAcctId(Long acctId);

    List<TransactionCategoryBalance> findByAcctIdOrderByAcctIdAscTypeCdAscCatCdAsc(Long acctId);
}
