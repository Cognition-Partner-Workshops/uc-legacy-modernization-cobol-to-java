package com.carddemo.repository;

import com.carddemo.model.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction Category reference data.
 * Replaces VSAM KSDS on TRANCATG file.
 */
@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Integer> {
}
