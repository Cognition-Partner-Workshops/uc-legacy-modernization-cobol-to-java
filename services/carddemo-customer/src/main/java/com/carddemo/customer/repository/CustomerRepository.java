package com.carddemo.customer.repository;

import com.carddemo.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE "
            + "(:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND "
            + "(:zip IS NULL OR c.zip = :zip)")
    Page<Customer> search(@Param("lastName") String lastName,
                          @Param("zip") String zip,
                          Pageable pageable);
}
