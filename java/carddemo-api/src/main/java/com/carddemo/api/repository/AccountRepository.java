package com.carddemo.api.repository;

import com.carddemo.common.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Account entity.
 * Replaces VSAM file I/O for ACCTDAT (Account Data file).
 * Maps to COBOL copybook: CVACT01Y.cpy
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
