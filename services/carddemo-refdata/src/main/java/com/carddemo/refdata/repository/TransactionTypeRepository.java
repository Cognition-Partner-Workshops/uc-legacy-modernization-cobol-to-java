package com.carddemo.refdata.repository;

import com.carddemo.refdata.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}
