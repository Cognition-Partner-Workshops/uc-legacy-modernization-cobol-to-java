package com.carddemo.repository;

import com.carddemo.entity.TranCatBalance;
import com.carddemo.entity.TranCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for TranCatBalance entity - replaces TCATBAL VSAM file operations.
 * Used by CBTRN02C batch program for maintaining category balances.
 */
@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {

    List<TranCatBalance> findByAccountId(Long accountId);
}
