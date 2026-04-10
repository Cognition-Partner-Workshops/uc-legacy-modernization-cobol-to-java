package com.carddemo.repository;

import com.carddemo.model.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    Optional<CardXref> findByXrefAcctId(Long xrefAcctId);

    Optional<CardXref> findByXrefCardNum(String xrefCardNum);
}
