package com.carddemo.repository;

import com.carddemo.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Card Cross Reference entity.
 * Replaces VSAM KSDS READ operations on CARDXREF (DD name XREFFILE).
 * The original VSAM had an Alternate Index (AIX) on XREF-ACCT-ID; here replaced by findByXrefAcctId.
 * Used by online programs: COACTVWC
 * Used by batch programs: CBTRN02C, CBACT04C, CBSTM03A
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByXrefAcctId(Long acctId);

    List<CardXref> findByXrefCustId(Long custId);
}
