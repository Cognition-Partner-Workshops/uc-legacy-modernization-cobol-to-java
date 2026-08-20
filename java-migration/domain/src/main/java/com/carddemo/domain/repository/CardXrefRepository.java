package com.carddemo.domain.repository;

import com.carddemo.domain.entity.CardXref;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    List<CardXref> findByCustId(BigDecimal custId);
    List<CardXref> findByAcctId(BigDecimal acctId);
}
