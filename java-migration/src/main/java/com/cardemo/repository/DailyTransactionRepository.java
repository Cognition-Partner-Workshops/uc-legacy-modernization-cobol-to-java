/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.repository;

import com.cardemo.model.DailyTransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Daily Transaction data access - replaces VSAM DALYTRAN file I/O.
 * Migrated from COBOL batch READ/WRITE on DALYTRAN dataset.
 */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransactionRecord, String> {
}
