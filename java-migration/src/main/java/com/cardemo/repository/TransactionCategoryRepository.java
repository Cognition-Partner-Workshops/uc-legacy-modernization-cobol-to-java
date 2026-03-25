/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.repository;

import com.cardemo.model.TransactionCategoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction Category data access - replaces VSAM TRANCATG file.
 * Migrated from COBOL batch I/O on TRANCATG dataset.
 */
@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategoryRecord, TransactionCategoryRecord.TranCatKey> {
}
