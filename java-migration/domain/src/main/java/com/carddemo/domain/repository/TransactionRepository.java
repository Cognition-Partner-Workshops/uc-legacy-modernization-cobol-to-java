package com.carddemo.domain.repository;

import com.carddemo.domain.entity.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByCardNum(String cardNum);
}
