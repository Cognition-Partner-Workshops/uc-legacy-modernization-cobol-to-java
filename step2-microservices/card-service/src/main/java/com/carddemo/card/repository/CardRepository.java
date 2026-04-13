package com.carddemo.card.repository;

import com.carddemo.card.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    List<Card> findByCardAcctId(Long cardAcctId);
}
