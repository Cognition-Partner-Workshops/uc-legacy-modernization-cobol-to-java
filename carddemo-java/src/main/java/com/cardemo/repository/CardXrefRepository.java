package com.cardemo.repository;

import com.cardemo.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CardXref entity - replaces VSAM KSDS file access
 * (XREFFILE in COBOL programs)
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByAcctId(Long acctId);

    List<CardXref> findByCustId(Long custId);

    Optional<CardXref> findByCardNum(String cardNum);
}
