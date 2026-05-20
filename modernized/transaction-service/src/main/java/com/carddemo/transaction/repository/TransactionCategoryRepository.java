package com.carddemo.transaction.repository;

import com.carddemo.transaction.model.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, TransactionCategory.TransactionCategoryId> {
}
