package com.cardemo.common.repository;

import com.cardemo.common.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for CardXref entity (CVACT03Y.cpy → card_xref table).
 *
 * TODO: Add cross-reference lookup used in COCRDLIC.cbl
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByXrefCustId(Long custId);

    List<CardXref> findByXrefAcctId(Long acctId);
}
