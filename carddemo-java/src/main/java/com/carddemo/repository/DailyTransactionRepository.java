package com.carddemo.repository;

import com.carddemo.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for DailyTransaction staging entity - replaces DALYTRAN file operations.
 * Used by batch transaction posting job (CBTRN02C).
 */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {

    List<DailyTransaction> findByStatus(String status);
}
