package com.carddemo.repository;

import com.carddemo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Customer entity - replaces CUSTDAT VSAM file operations.
 * Consolidates file I/O from COACTVWC, COACTUPC, COCRDSLC, CBSTM03B.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
