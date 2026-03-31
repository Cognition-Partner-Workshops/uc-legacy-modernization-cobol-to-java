package com.carddemo.repository;

import com.carddemo.model.TransactionCategoryBalance;
import com.carddemo.model.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {
    List<TransactionCategoryBalance> findByAcctIdOrderByTypeCdAscCatCdAsc(Long acctId);
    List<TransactionCategoryBalance> findAllByOrderByAcctIdAscTypeCdAscCatCdAsc();
}
