package com.carddemo.repository;

import com.carddemo.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CardXref entity - replaces CCXREF VSAM file + CXACAIX alternate index.
 * Consolidates file I/O from COACTVWC, COTRN02C, COBIL00C, CBTRN02C.
 * The findByAccountId method replaces the CXACAIX alternate index.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByAccountId(Long accountId);

    Optional<CardXref> findByCardNumber(String cardNumber);

    List<CardXref> findByCustomerId(Long customerId);
}
