package com.cardemo.repository;

import com.cardemo.model.CardXref;
import java.util.Optional;

/**
 * Repository interface for card cross-reference data access.
 * Equivalent of COBOL CCXREF / CXACAIX VSAM file operations.
 */
public interface CardXrefRepository {

    Optional<CardXref> findByCardNumber(String cardNumber);

    Optional<CardXref> findByAccountId(long accountId);
}
