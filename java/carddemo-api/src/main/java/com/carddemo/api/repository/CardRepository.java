package com.carddemo.api.repository;

import com.carddemo.common.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Card entity.
 * Replaces VSAM file I/O for CARDDAT (Card Data file).
 * Maps to COBOL copybook: CVACT02Y.cpy
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {
}
