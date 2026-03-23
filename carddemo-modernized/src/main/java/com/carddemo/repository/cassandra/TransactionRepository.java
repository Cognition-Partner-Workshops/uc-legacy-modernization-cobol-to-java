package com.carddemo.repository.cassandra;

import com.carddemo.model.Transaction;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive Cassandra repository for Transaction entity.
 *
 * Replaces legacy VSAM KSDS file operations on TRANSACT:
 *   EXEC CICS READ DATASET('TRANSACT')  -> findById()
 *   EXEC CICS WRITE DATASET('TRANSACT') -> save()
 *   EXEC CICS STARTBR/READNEXT/READPREV/ENDBR -> findByCardNumber(), findAll()
 *
 * Cassandra is ideal for this high-throughput, time-series transaction data.
 *
 * Programs modernized: COTRN00C (Transaction List), COTRN01C (Transaction View),
 *                       COTRN02C (Transaction Add), CBTRN02C (Batch Transaction Processing),
 *                       CBTRN03C (Transaction Report)
 */
@Repository
public interface TransactionRepository extends ReactiveCassandraRepository<Transaction, String> {

    @Query("SELECT * FROM transactions WHERE card_number = ?0 ALLOW FILTERING")
    Flux<Transaction> findByCardNumber(String cardNumber);

    @Query("SELECT * FROM transactions WHERE type_code = ?0 ALLOW FILTERING")
    Flux<Transaction> findByTypeCode(String typeCode);
}
