package com.carddemo.api.repository;

import com.carddemo.common.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Customer entity.
 * Replaces VSAM file I/O for CUSTDAT (Customer Data file).
 * Maps to COBOL copybook: CVCUS01Y.cpy
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
