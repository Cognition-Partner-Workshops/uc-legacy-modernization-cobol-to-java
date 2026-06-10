package com.carddemo.repository;

import com.carddemo.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByLastNameIgnoreCase(String lastName);

    Page<Customer> findByAddressStateCode(String stateCode, Pageable pageable);
}
