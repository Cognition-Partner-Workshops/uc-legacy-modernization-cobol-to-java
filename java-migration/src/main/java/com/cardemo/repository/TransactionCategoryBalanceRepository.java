package com.cardemo.repository;

import com.cardemo.model.TransactionCategoryBalanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Transaction Category Balance data access - replaces VSAM TCATBALF file.
 * Migrated from COBOL batch I/O on TCATBALF dataset.
 */
@Repository
public interface TransactionCategoryBalanceRepository extends JpaRepository<TransactionCategoryBalanceRecord, TransactionCategoryBalanceRecord.TranCatBalKey> {

    List<TransactionCategoryBalanceRecord> findByTrancatAcctId(long acctId);
}
