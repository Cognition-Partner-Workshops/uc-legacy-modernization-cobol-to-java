package com.carddemo.repository;

import com.carddemo.model.entity.TransactionCatBal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCatBalRepository extends JpaRepository<TransactionCatBal, TransactionCatBal.TransactionCatBalId> {

    List<TransactionCatBal> findAllByOrderByTrancatAcctIdAscTrancatTypeCdAscTrancatCdAsc();

    List<TransactionCatBal> findByTrancatAcctId(Long trancatAcctId);
}
