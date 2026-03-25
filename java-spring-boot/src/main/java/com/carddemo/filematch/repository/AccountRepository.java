package com.carddemo.filematch.repository;

import com.carddemo.filematch.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Account entities.
 * Replaces VSAM KSDS indexed file access from COBOL CBACT01C.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    /** Find all accounts by active status (equivalent to COBOL match on ACCT-ACTIVE-STATUS). */
    List<Account> findByAcctActiveStatus(String acctActiveStatus);

    /** Find all accounts in a specific group. */
    List<Account> findByAcctGroupId(String acctGroupId);
}
