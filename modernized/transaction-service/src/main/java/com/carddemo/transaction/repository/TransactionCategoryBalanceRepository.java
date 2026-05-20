package com.carddemo.transaction.repository;

import com.carddemo.transaction.model.TransactionCategoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionCategoryBalanceRepository extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalance.TranCatBalanceId> {
    Optional<TransactionCategoryBalance> findByAcctIdAndTypeCdAndCatCd(String acctId, String typeCd, Integer catCd);
    List<TransactionCategoryBalance> findByAcctId(String acctId);
}
