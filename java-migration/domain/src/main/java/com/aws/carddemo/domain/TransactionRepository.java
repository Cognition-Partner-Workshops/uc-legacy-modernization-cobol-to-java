package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
  java.util.List<Transaction> findByTranIdGreaterThanOrderByTranId(String id);

  java.util.List<Transaction> findByCardNumOrderByTranId(String cardNum);

  java.util.List<Transaction> findByTypeCdAndCatCd(String typeCd, Integer catCd);
}
