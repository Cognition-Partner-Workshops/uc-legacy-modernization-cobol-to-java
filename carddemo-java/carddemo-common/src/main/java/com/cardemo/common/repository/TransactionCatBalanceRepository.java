package com.cardemo.common.repository;

import com.cardemo.common.entity.TransactionCatBalance;
import com.cardemo.common.entity.TransactionCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for TransactionCatBalance entity (CVTRA01Y.cpy → tran_cat_balance table).
 *
 * TODO: Used by CBACT04C.cbl (Interest Calculation batch) to update category balances
 */
@Repository
public interface TransactionCatBalanceRepository extends JpaRepository<TransactionCatBalance, TransactionCatBalanceId> {

    List<TransactionCatBalance> findByAcctId(Long acctId);
}
