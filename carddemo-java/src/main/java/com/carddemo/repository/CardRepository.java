package com.carddemo.repository;

import com.carddemo.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Card entity.
 * Replaces VSAM KSDS READ/STARTBR/READNEXT operations on CARDDAT (DD name CARDFILE).
 * The original VSAM had an Alternate Index (AIX) on CARD-ACCT-ID; here replaced by findByCardAcctId.
 * Used by online programs: COCRDLIC, COCRDSLC, COCRDUPC
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    List<Card> findByCardAcctId(Long acctId);

    List<Card> findByCardActiveStatus(String status);
}
