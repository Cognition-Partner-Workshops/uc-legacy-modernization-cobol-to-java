package com.carddemo.repository;

import com.carddemo.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByAccountId(String accountId);

    // Equivalent to 9000-READ-FORWARD / 9100-READ-BACKWARDS in COCRDLIC.cbl
    // COBOL uses STARTBR + READNEXT/READPREV for 7-row page navigation.
    // Spring Data Pageable provides equivalent PF7/PF8 pagination.
    Page<Card> findByAccountId(String accountId, Pageable pageable);
}
