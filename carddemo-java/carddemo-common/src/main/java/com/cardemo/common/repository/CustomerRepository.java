package com.cardemo.common.repository;

import com.cardemo.common.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for Customer entity (CVCUS01Y.cpy → customers table).
 *
 * TODO: Add customer search by name/SSN used in card management screens
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
}
