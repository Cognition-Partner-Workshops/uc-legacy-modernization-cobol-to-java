package com.carddemo.batch.repository;

import com.carddemo.common.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByProcessedTimestampBetweenOrderByCardNumberAscTransactionIdAsc(
            LocalDateTime start, LocalDateTime end);

    List<Transaction> findAllByOrderByCardNumberAscTransactionIdAsc();
}
