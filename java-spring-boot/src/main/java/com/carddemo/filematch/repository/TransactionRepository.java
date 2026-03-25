package com.carddemo.filematch.repository;

import com.carddemo.filematch.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository for Transaction entities.
 * Replaces VSAM indexed file access from COBOL CBTRN02C.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /** Find transactions by type code (equivalent to COBOL match on TRAN-TYPE-CD). */
    List<Transaction> findByTranTypeCd(String tranTypeCd);

    /** Find transactions by card number. */
    List<Transaction> findByTranCardNum(String tranCardNum);

    /** Find transactions by category code. */
    List<Transaction> findByTranCatCd(Integer tranCatCd);
}
