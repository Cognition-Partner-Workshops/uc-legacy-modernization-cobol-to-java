package com.cardemo.repository;

import com.cardemo.model.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DailyTransaction entity - replaces VSAM sequential file access
 * (DALYTRAN in COBOL programs)
 */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {

    List<DailyTransaction> findByCardNum(String cardNum);
}
