package com.carddemo.repository;

import com.carddemo.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Card entity - replaces CARDDAT VSAM file + CARDAIX alternate index.
 * Consolidates file I/O from COCRDLIC, COCRDSLC, COCRDUPC.
 * The findByAccountId method replaces the CARDAIX alternate index browse.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByAccountId(Long accountId);

    Page<Card> findByAccountId(Long accountId, Pageable pageable);
}
