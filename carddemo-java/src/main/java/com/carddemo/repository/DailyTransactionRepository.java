package com.carddemo.repository;

import com.carddemo.model.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Daily Transaction entity.
 * Replaces sequential READ on DALYTRAN file (DD name DALYTRAN).
 * Used by batch program CBTRN02C for daily transaction posting.
 */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, Long> {

    List<DailyTransaction> findByProcessedFalse();

    List<DailyTransaction> findByDalytranCardNum(String cardNum);
}
