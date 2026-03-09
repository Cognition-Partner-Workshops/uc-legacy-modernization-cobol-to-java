package com.carddemo.repository;

import com.carddemo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Customer entity.
 * Replaces VSAM KSDS READ operations on CUSTDAT (DD name CUSTFILE).
 * Used by online programs: COACTVWC, CBSTM03A
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByCustLastName(String lastName);

    List<Customer> findByCustAddrStateCd(String stateCd);
}
