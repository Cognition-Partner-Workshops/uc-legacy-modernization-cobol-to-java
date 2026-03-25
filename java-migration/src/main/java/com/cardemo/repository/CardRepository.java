/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.repository;

import com.cardemo.model.CardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Card data access - replaces VSAM CARDDAT file I/O.
 * Migrated from COBOL CICS READ/BROWSE on CARDDAT and CARDAIX datasets.
 */
@Repository
public interface CardRepository extends JpaRepository<CardRecord, String> {

    List<CardRecord> findByCardAcctId(long acctId);
}
