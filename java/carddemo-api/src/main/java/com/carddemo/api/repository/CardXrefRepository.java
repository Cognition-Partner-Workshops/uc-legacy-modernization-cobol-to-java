package com.carddemo.api.repository;

import com.carddemo.common.model.CardCrossReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for CardCrossReference entity.
 * Replaces VSAM file I/O for CARDXREF (Card Cross-Reference file).
 * Maps to COBOL copybook: CVACT03Y.cpy
 *
 * The findByAccountId method replaces the CXACAIX alternate index
 * used in COBOL programs to look up cards by account ID.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardCrossReference, String> {

    /**
     * Find all cross-references by account ID.
     * Replaces the CXACAIX alternate index on the CARDXREF VSAM file.
     */
    List<CardCrossReference> findByAccountId(Long accountId);
}
