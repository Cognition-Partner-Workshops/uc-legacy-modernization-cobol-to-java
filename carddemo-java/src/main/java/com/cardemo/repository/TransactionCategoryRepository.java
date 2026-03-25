package com.cardemo.repository;

import com.cardemo.model.TransactionCategory;
import com.cardemo.model.TransactionCategoryKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TransactionCategory entity - replaces VSAM KSDS file access
 */
@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, TransactionCategoryKey> {

    List<TransactionCategory> findByTypeCd(String typeCd);
}
