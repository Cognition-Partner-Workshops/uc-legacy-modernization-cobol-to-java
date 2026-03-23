package com.carddemo.repository.cassandra;

import com.carddemo.model.Card;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive Cassandra repository for Card entity.
 *
 * Replaces legacy VSAM KSDS file operations on CARDDAT:
 *   EXEC CICS READ DATASET('CARDDAT')  -> findById()
 *   EXEC CICS WRITE DATASET('CARDDAT') -> save()
 *   EXEC CICS STARTBR/READNEXT/ENDBR   -> findByAccountId()
 *
 * Programs modernized: COCRDLIC (Card List), COCRDSLC (Card View),
 *                       COCRDUPC (Card Update)
 */
@Repository
public interface CardRepository extends ReactiveCassandraRepository<Card, String> {

    @Query("SELECT * FROM cards WHERE account_id = ?0 ALLOW FILTERING")
    Flux<Card> findByAccountId(String accountId);

    @Query("SELECT * FROM cards WHERE active_status = ?0 ALLOW FILTERING")
    Flux<Card> findByActiveStatus(String activeStatus);
}
