package com.carddemo.domain.repository;

import com.carddemo.domain.entity.TranCatBal;
import com.carddemo.domain.entity.TranCatBalId;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranCatBalRepository extends JpaRepository<TranCatBal, TranCatBalId> {
    List<TranCatBal> findByIdAcctId(BigDecimal acctId);
}
