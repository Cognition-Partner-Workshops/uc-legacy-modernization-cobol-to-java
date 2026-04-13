package com.carddemo.repository;

import com.carddemo.entity.TranCatBalanceId;
import com.carddemo.entity.TransactionCategoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TranCatBalanceId> {
    List<TransactionCategoryBalance> findByAcctId(Long acctId);
}
