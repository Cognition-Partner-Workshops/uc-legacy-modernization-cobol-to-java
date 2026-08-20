package com.aws.carddemo.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionTypeCategoryRepository
    extends JpaRepository<TransactionTypeCategory, TransactionTypeCategoryId> {}
