package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCategoryRepository
    extends JpaRepository<TransactionCategory, TransactionCategoryId> {
  java.util.List<TransactionCategory> findByIdTypeCd(String typeCd);

  java.util.List<TransactionCategory> findByIdCatCd(Integer catCd);
}
