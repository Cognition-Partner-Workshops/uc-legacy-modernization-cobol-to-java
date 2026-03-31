package com.carddemo.service;

import com.carddemo.exception.CustomerNotFoundException;
import com.carddemo.model.Customer;
import com.carddemo.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Optional<Customer> findById(Long custId) {
        return customerRepository.findById(custId);
    }

    public Customer getCustomer(Long custId) {
        return customerRepository.findById(custId)
                .orElseThrow(() -> new CustomerNotFoundException(custId));
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }
}
