package com.carddemo.batch.repository;

import com.carddemo.common.model.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCategoryRepository
        extends JpaRepository<TransactionCategory, TransactionCategory.TransactionCategoryId> {

    java.util.Optional<TransactionCategory> findByTypeCodeAndCategoryCode(
            String typeCode, String categoryCode);
}
