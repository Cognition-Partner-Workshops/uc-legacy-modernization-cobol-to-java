package com.carddemo.card.repository;

import com.carddemo.card.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    Optional<CardXref> findByXrefCardNum(String xrefCardNum);
    Optional<CardXref> findByXrefAcctId(Long xrefAcctId);
}
