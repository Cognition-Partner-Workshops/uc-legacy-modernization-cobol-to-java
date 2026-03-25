package com.cardemo.repository;

import com.cardemo.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Transaction entity - replaces VSAM KSDS file access
 * (TRANFILE in COBOL programs)
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByCardNum(String cardNum);

    Page<Transaction> findByCardNum(String cardNum, Pageable pageable);

    List<Transaction> findByTypeCd(String typeCd);
}
