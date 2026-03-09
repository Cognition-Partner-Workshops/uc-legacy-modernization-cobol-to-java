package com.carddemo.repository;

import com.carddemo.model.TranCatBalance;
import com.carddemo.model.TranCatBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Transaction Category Balance entity.
 * Replaces VSAM KSDS sequential READ/REWRITE operations on TCATBALF.
 * Used by batch programs: CBTRN02C (posting), CBACT04C (interest calc)
 */
@Repository
public interface TranCatBalanceRepository extends JpaRepository<TranCatBalance, TranCatBalanceId> {

    List<TranCatBalance> findByTrancatAcctId(Long acctId);

    List<TranCatBalance> findByTrancatAcctIdOrderByTrancatTypeCdAscTrancatCdAsc(Long acctId);
}
