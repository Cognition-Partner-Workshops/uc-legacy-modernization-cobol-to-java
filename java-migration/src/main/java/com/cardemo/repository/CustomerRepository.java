/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.repository;

import com.cardemo.model.CustomerRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Customer data access - replaces VSAM CUSTDAT file I/O.
 * Migrated from COBOL CICS READ/WRITE on CUSTDAT dataset.
 */
@Repository
public interface CustomerRepository extends JpaRepository<CustomerRecord, Long> {
}
