package com.carddemo.transaction.repository;

import com.carddemo.transaction.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByCardNum(String cardNum, Pageable pageable);
    Optional<Transaction> findByTranId(String tranId);
}
