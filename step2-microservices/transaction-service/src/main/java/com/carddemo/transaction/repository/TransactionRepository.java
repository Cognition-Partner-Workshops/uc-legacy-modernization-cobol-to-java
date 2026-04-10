package com.carddemo.transaction.repository;

import com.carddemo.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByTranCardNumOrderByTranIdDesc(String tranCardNum);

    @Query("SELECT MAX(t.tranId) FROM Transaction t")
    Optional<String> findMaxTranId();
}
