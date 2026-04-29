package com.cardemo.common.repository;

import com.cardemo.common.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DailyTransaction entity (CVTRA06Y.cpy → daily_transactions table).
 *
 * TODO: Used by POSTTRAN batch job (CBTRN02C.cbl) to read daily transactions for posting
 */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {
}
