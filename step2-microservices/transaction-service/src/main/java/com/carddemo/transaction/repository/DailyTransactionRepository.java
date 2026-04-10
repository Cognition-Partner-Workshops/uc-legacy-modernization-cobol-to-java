package com.carddemo.transaction.repository;

import com.carddemo.transaction.model.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {
    List<DailyTransaction> findAllByOrderByDalytranIdAsc();
}
