package com.carddemo.api.repository;

import com.carddemo.common.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Transaction entity.
 * Replaces VSAM file I/O for TRANSACT (Transaction file).
 * Maps to COBOL copybook: CVTRA05Y.cpy
 *
 * The findAll with Pageable replaces the STARTBR/READNEXT/READPREV
 * CICS browsing commands used in COTRN00C for page-forward/backward navigation.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Paginated transaction retrieval.
     * Replaces COTRN00C STARTBR/READNEXT/READPREV browsing of TRANSACT VSAM file.
     */
    @Override
    Page<Transaction> findAll(Pageable pageable);
}
