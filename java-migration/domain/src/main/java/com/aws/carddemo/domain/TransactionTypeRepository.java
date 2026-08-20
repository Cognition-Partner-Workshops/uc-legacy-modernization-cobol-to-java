package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
  java.util.List<TransactionType> findByTypeCdGreaterThanOrderByTypeCd(String typeCd);
}
