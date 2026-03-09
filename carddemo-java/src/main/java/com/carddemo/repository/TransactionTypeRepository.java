package com.carddemo.repository;

import com.carddemo.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction Type reference data.
 * Replaces VSAM KSDS on TRANTYPE file.
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}
