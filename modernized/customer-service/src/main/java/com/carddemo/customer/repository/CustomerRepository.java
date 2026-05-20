package com.carddemo.customer.repository;

import com.carddemo.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findBySsn(String ssn);
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
}
