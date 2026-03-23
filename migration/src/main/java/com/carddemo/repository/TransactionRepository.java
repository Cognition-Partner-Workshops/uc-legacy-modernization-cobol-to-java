package com.carddemo.repository;

import com.carddemo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByCardNum(String cardNum);
    Page<Transaction> findByCardNum(String cardNum, Pageable pageable);
    Page<Transaction> findByCardNumIn(List<String> cardNums, Pageable pageable);
}
