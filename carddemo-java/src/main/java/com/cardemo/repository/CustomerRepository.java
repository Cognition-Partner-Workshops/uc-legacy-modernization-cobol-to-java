package com.cardemo.repository;

import com.cardemo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Customer entity - replaces VSAM KSDS file access
 * (CUSTFILE in COBOL programs)
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);

    List<Customer> findBySsn(Long ssn);
}
