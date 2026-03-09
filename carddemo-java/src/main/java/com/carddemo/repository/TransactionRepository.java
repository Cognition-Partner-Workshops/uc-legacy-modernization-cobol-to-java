package com.carddemo.repository;

import com.carddemo.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Transaction entity.
 * Replaces VSAM KSDS READ/WRITE/STARTBR/READNEXT operations on TRANSACT (DD name TRANFILE).
 * The original VSAM had an Alternate Index (AIX) on TRAN-CARD-NUM.
 * Used by online programs: COTRN00C, COTRN01C, COTRN02C
 * Used by batch programs: CBTRN02C, CBACT04C, CBSTM03A
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByTranCardNum(String cardNum);

    Page<Transaction> findByTranCardNum(String cardNum, Pageable pageable);

    List<Transaction> findByTranTypeCd(String typeCd);

    List<Transaction> findByTranCardNumIn(List<String> cardNums);
}
