package com.carddemo.domain.repository;

import com.carddemo.domain.entity.Customer;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, BigDecimal> {
}
