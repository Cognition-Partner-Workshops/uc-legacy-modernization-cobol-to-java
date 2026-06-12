package com.carddemo.refdata.repository;

import java.util.List;

import com.carddemo.refdata.entity.TransactionCategory;
import com.carddemo.refdata.entity.TransactionCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, TransactionCategoryId> {

    List<TransactionCategory> findByTypeCode(String typeCode);
}
