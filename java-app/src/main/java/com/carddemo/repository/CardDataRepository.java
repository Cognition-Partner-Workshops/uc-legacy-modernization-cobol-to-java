package com.carddemo.repository;

import com.carddemo.model.CardData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardDataRepository extends JpaRepository<CardData, String> {
    List<CardData> findByAcctId(Long acctId);
    List<CardData> findAllByOrderByCardNumAsc();
}
