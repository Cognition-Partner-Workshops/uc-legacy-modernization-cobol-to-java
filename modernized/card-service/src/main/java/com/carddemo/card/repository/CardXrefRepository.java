package com.carddemo.card.repository;

import com.carddemo.card.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    Optional<CardXref> findByAcctId(String acctId);
    List<CardXref> findByCustId(String custId);
}
