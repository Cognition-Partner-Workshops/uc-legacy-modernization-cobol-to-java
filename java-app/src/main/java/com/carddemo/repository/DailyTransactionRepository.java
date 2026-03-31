package com.carddemo.repository;

import com.carddemo.model.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {
    List<DailyTransaction> findAllByOrderByTranIdAsc();
}
