package com.carddemo.repository;

import com.carddemo.model.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByTranCardNumOrderByTranIdDesc(String tranCardNum, Pageable pageable);

    List<Transaction> findByTranCardNumOrderByTranIdAsc(String tranCardNum);

    Page<Transaction> findAllByOrderByTranIdDesc(Pageable pageable);

    @Query("SELECT t FROM Transaction t ORDER BY t.tranId DESC")
    List<Transaction> findTopByOrderByTranIdDesc(Pageable pageable);

    @Query("SELECT MAX(t.tranId) FROM Transaction t")
    Optional<String> findMaxTranId();
}
