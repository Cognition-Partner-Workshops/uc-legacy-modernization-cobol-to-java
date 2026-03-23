package com.carddemo.repository.cassandra;

import com.carddemo.model.Account;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive Cassandra repository for Account entity.
 *
 * Replaces legacy VSAM KSDS file operations on ACCTDAT:
 *   EXEC CICS READ DATASET('ACCTDAT')  -> findById()
 *   EXEC CICS WRITE DATASET('ACCTDAT') -> save()
 *   EXEC CICS REWRITE                  -> save() (upsert)
 *   EXEC CICS STARTBR/READNEXT/ENDBR   -> findAll() / custom queries
 *
 * Programs modernized: COACTVWC (Account View), COACTUPC (Account Update),
 *                       CBACT01C-CBACT04C (Batch Account Processing)
 */
@Repository
public interface AccountRepository extends ReactiveCassandraRepository<Account, String> {

    @Query("SELECT * FROM accounts WHERE active_status = ?0 ALLOW FILTERING")
    Flux<Account> findByActiveStatus(String activeStatus);

    @Query("SELECT * FROM accounts WHERE group_id = ?0 ALLOW FILTERING")
    Flux<Account> findByGroupId(String groupId);
}
