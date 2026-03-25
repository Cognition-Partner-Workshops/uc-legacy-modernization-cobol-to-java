/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.repository;

import com.cardemo.model.TransactionTypeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction Type data access - replaces VSAM TRANTYPE file.
 * Migrated from COBOL CICS READ/WRITE/REWRITE/DELETE and batch I/O on TRANTYPE.
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionTypeRecord, String> {
}
