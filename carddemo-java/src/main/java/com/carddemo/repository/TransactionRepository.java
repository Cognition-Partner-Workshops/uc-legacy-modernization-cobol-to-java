package com.carddemo.repository;

import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, TransactionId> {

    List<Transaction> findByCardNum(String cardNum);

    Page<Transaction> findByCardNum(String cardNum, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.cardNum = :cardNum " +
           "AND t.origTimestamp >= :startDate AND t.origTimestamp <= :endDate")
    List<Transaction> findByCardNumAndDateRange(
            @Param("cardNum") String cardNum,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
}
