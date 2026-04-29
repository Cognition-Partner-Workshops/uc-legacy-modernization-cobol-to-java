package com.cardemo.common.repository;

import com.cardemo.common.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for TransactionType entity (CVTRA03Y.cpy → transaction_types table).
 *
 * TODO: Used by DB2 extension programs COTRTUPC.cbl and COTRTLIC.cbl
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, String> {
}
