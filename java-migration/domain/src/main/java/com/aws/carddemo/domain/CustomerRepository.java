package com.aws.carddemo.domain;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CustomerRepository extends JpaRepository<Customer,Integer> { java.util.List<Customer> findByCustIdGreaterThanOrderByCustId(Integer id); }
