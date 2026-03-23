package com.carddemo.repository.cassandra;

import com.carddemo.model.UserSecurity;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive Cassandra repository for User Security entity.
 *
 * Replaces legacy VSAM KSDS file operations on USRSEC:
 *   EXEC CICS READ DATASET('USRSEC')  -> findById()
 *   EXEC CICS WRITE DATASET('USRSEC') -> save()
 *   EXEC CICS DELETE DATASET('USRSEC') -> deleteById()
 *
 * Programs modernized: COSGN00C (Sign-on authentication),
 *                       COUSR00C-COUSR03C (Admin User Management)
 */
@Repository
public interface UserSecurityRepository extends ReactiveCassandraRepository<UserSecurity, String> {

    @Query("SELECT * FROM users WHERE user_type = ?0 ALLOW FILTERING")
    Flux<UserSecurity> findByUserType(String userType);
}
