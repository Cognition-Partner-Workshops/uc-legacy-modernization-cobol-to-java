package com.carddemo.repository;

import com.carddemo.model.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByCardAcctId(Long cardAcctId);

    Page<Card> findAllByOrderByCardNumAsc(Pageable pageable);

    List<Card> findAllByOrderByCardNumAsc();
}
