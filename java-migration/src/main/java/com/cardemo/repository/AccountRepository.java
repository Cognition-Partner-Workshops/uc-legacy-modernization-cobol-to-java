package com.cardemo.repository;

import com.cardemo.model.AccountRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Account data access - replaces VSAM ACCTDAT file I/O.
 * Migrated from COBOL CICS READ/WRITE/REWRITE on ACCTDAT dataset.
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountRecord, Long> {
}
