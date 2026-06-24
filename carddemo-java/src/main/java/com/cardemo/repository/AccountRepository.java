package com.cardemo.repository;

import com.cardemo.model.Account;
import java.util.Optional;

/**
 * Repository interface for account data access.
 * Equivalent of COBOL ACCTDAT VSAM file operations.
 */
public interface AccountRepository {

    Optional<Account> findByAccountId(long accountId);

    void update(Account account);
}
