package com.cardemo.repository;

import com.cardemo.model.TransactionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Transaction data access - replaces VSAM TRANSACT file I/O.
 * Migrated from COBOL CICS STARTBR/READNEXT/READPREV/ENDBR on TRANSACT dataset.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionRecord, String> {

    List<TransactionRecord> findByTranCardNum(String cardNum);

    Page<TransactionRecord> findByTranIdGreaterThanEqualOrderByTranIdAsc(String tranId, Pageable pageable);

    Page<TransactionRecord> findByTranIdLessThanEqualOrderByTranIdDesc(String tranId, Pageable pageable);
}
