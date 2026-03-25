package com.cardemo.repository;

import com.cardemo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Account entity - replaces VSAM KSDS file access
 * (ACCTFILE in COBOL programs)
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByActiveStatus(String status);

    List<Account> findByGroupId(String groupId);
}
