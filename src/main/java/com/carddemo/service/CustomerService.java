package com.carddemo.service;

import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Customer;
import com.carddemo.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Customer findById(long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Transactional
    public Customer create(Customer customer) {
        if (customer.getCustId() != 0 && customerRepository.existsById(customer.getCustId())) {
            throw new IllegalArgumentException("Customer already exists with id: " + customer.getCustId());
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer update(long id, Customer updated) {
        Customer existing = findById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setMiddleName(updated.getMiddleName());
        existing.setLastName(updated.getLastName());
        existing.setAddressLine1(updated.getAddressLine1());
        existing.setAddressLine2(updated.getAddressLine2());
        existing.setAddressLine3(updated.getAddressLine3());
        existing.setAddressStateCode(updated.getAddressStateCode());
        existing.setAddressCountryCode(updated.getAddressCountryCode());
        existing.setAddressZip(updated.getAddressZip());
        existing.setPhoneNumber1(updated.getPhoneNumber1());
        existing.setPhoneNumber2(updated.getPhoneNumber2());
        existing.setSsn(updated.getSsn());
        existing.setGovtIssuedId(updated.getGovtIssuedId());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setEftAccountId(updated.getEftAccountId());
        existing.setPrimaryCardHolderIndicator(updated.getPrimaryCardHolderIndicator());
        existing.setFicoCreditScore(updated.getFicoCreditScore());
        return customerRepository.save(existing);
    }
}
