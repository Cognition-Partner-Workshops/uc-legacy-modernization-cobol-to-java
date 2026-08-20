package com.carddemo.domain.repository;

import com.carddemo.domain.entity.Card;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, String> {
    List<Card> findByAcctId(BigDecimal acctId);
}
