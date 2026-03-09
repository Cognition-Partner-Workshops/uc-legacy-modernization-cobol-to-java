package com.carddemo.repository;

import com.carddemo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Account entity.
 * Replaces VSAM KSDS READ/WRITE/REWRITE operations on ACCTDAT (DD name ACCTFILE).
 * Used by online programs: COACTVWC, COACTUPC
 * Used by batch programs: CBTRN02C, CBACT04C
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByAcctActiveStatus(String status);

    List<Account> findByAcctGroupId(String groupId);

    List<Account> findByAcctAddrZip(String zip);
}
