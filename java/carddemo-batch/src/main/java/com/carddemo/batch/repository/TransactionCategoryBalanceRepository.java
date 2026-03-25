package com.carddemo.batch.repository;

import com.carddemo.common.model.TransactionCategoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalance.TransactionCategoryBalanceId> {

    List<TransactionCategoryBalance> findAllByOrderByAccountIdAscTypeCodeAscCategoryCodeAsc();

    Optional<TransactionCategoryBalance> findByAccountIdAndTypeCodeAndCategoryCode(
            Long accountId, String typeCode, String categoryCode);
}
