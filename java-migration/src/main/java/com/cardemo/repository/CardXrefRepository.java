/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.repository;

import com.cardemo.model.CardXrefRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Card Cross-Reference data access - replaces VSAM CARDXREF file I/O.
 * Migrated from COBOL CICS READ on CXACAIX (alternate index by account) dataset.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXrefRecord, String> {

    List<CardXrefRecord> findByXrefAcctId(long acctId);

    List<CardXrefRecord> findByXrefCustId(long custId);
}
