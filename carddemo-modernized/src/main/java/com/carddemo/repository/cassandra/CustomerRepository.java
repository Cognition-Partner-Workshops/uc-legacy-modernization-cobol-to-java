package com.carddemo.repository.cassandra;

import com.carddemo.model.Customer;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Reactive Cassandra repository for Customer entity.
 *
 * Replaces legacy VSAM KSDS file operations on CUSTDAT:
 *   EXEC CICS READ DATASET('CUSTDAT')  -> findById()
 *   EXEC CICS WRITE DATASET('CUSTDAT') -> save()
 *
 * Programs modernized: CBCUS01C (Batch Customer Processing),
 *                       COACTVWC (reads customer data for account view)
 */
@Repository
public interface CustomerRepository extends ReactiveCassandraRepository<Customer, String> {

    @Query("SELECT * FROM customers WHERE last_name = ?0 ALLOW FILTERING")
    Flux<Customer> findByLastName(String lastName);

    @Query("SELECT * FROM customers WHERE state_code = ?0 ALLOW FILTERING")
    Flux<Customer> findByStateCode(String stateCode);
}
