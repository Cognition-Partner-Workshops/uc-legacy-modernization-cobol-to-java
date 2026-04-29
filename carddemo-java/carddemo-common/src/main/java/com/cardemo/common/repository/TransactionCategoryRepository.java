package com.cardemo.common.repository;

import com.cardemo.common.entity.TransactionCategory;
import com.cardemo.common.entity.TransactionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for TransactionCategory entity (CVTRA04Y.cpy → transaction_categories table).
 */
@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, TransactionCategoryId> {

    List<TransactionCategory> findByTranTypeCd(String tranTypeCd);
}
