package com.cardemo.repository;

import com.cardemo.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for TransactionType entity - replaces VSAM KSDS file access
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}
