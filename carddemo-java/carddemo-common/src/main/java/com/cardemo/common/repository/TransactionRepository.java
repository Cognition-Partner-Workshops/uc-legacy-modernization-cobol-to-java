package com.cardemo.common.repository;

import com.cardemo.common.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction entity (CVTRA05Y.cpy → transactions table).
 *
 * TODO: Add queries for transaction list/search from COTRN00C.cbl
 * TODO: Add queries for transaction report from CORPT00C.cbl / CBTRN03C.cbl
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByCardNum(String cardNum, Pageable pageable);

    Page<Transaction> findByTranTypeCd(String tranTypeCd, Pageable pageable);
}
