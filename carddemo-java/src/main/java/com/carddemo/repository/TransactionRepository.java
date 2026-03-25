package com.carddemo.repository;

import com.carddemo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for Transaction entity - replaces TRANSACT VSAM file operations.
 * Consolidates file I/O from COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBTRN02C, CORPT00C.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findAllByOrderByTransactionIdDesc(Pageable pageable);

    Page<Transaction> findByCardNumberOrderByOriginatedTsDesc(String cardNumber, Pageable pageable);

    Page<Transaction> findByOriginatedTsBetweenOrderByOriginatedTsDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT MAX(t.transactionId) FROM Transaction t")
    Optional<String> findMaxTransactionId();

    @Query("SELECT t FROM Transaction t WHERE t.cardNumber IN " +
           "(SELECT c.cardNumber FROM Card c WHERE c.accountId = :accountId) " +
           "ORDER BY t.originatedTs DESC")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);
}
