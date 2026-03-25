package com.carddemo.repository;

import com.carddemo.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Account entity - replaces ACCTDAT VSAM file operations.
 * Consolidates file I/O from COACTVWC, COACTUPC, COBIL00C, COTRN02C, CBTRN02C, CBACT04C.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
