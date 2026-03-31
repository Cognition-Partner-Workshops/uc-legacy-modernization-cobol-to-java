package com.carddemo.repository;

import com.carddemo.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findAllByOrderByTranIdDesc(Pageable pageable);

    List<Transaction> findByCardNumOrderByTranIdDesc(String cardNum);

    @Query("SELECT t FROM Transaction t WHERE t.cardNum = :cardNum ORDER BY t.tranId DESC")
    Page<Transaction> findByCardNum(@Param("cardNum") String cardNum, Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(t.tranId AS long)), 0) FROM Transaction t")
    Long findMaxTranId();
}
