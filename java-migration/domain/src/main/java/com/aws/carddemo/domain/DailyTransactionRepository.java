package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {
  java.util.List<DailyTransaction> findByTranIdGreaterThanOrderByTranId(String id);

  java.util.List<DailyTransaction> findByCardNum(String cardNum);
}
