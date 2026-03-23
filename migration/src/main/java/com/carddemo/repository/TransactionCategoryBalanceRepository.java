package com.carddemo.repository;

import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.entity.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {

    List<TransactionCategoryBalance> findByAcctId(String acctId);

    Optional<TransactionCategoryBalance> findByAcctIdAndTypeCdAndCatCd(
            String acctId, String typeCd, Integer catCd);

    List<TransactionCategoryBalance> findAllByOrderByAcctIdAscTypeCdAscCatCdAsc();
}
