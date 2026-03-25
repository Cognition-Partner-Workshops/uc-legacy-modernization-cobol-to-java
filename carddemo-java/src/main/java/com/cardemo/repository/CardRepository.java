package com.cardemo.repository;

import com.cardemo.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Card entity - replaces VSAM KSDS file access
 * (CARDFILE in COBOL programs)
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByCardAcctId(Long acctId);

    List<Card> findByActiveStatus(String status);
}
