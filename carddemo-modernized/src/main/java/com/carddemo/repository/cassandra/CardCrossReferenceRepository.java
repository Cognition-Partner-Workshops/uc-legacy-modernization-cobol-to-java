package com.carddemo.repository.cassandra;

import com.carddemo.model.CardCrossReference;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive Cassandra repository for Card Cross-Reference entity.
 *
 * Replaces legacy VSAM KSDS file operations on CARDXREF with AIX (CXACAIX):
 *   EXEC CICS READ DATASET('CARDXREF')  -> findById()
 *   EXEC CICS READ DATASET('CXACAIX')   -> findByAccountId() (Alternate Index)
 *
 * Programs modernized: COACTVWC (reads cross-ref to find cards for an account)
 */
@Repository
public interface CardCrossReferenceRepository extends ReactiveCassandraRepository<CardCrossReference, String> {

    @Query("SELECT * FROM card_cross_references WHERE account_id = ?0 ALLOW FILTERING")
    Flux<CardCrossReference> findByAccountId(String accountId);

    @Query("SELECT * FROM card_cross_references WHERE customer_id = ?0 ALLOW FILTERING")
    Flux<CardCrossReference> findByCustomerId(String customerId);
}
