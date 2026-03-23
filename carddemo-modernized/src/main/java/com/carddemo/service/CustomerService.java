package com.carddemo.service;

import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Customer;
import com.carddemo.repository.cassandra.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Customer service - modernized business logic from COBOL programs:
 *
 *   CBCUS01C.cbl  -> batch customer data processing
 *   COACTVWC.cbl  -> customer lookup as part of account view
 *
 * Legacy: Customer data read from VSAM KSDS (CUSTDAT) using
 *   EXEC CICS READ DATASET('CUSTDAT') RIDFLD(customer-id)
 *
 * Modernized: Reactive Cassandra repository with Mono/Flux
 */
@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Flux<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Mono<Customer> getCustomerById(String customerId) {
        log.debug("Looking up customer: {}", customerId);
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Customer not found: " + customerId)));
    }

    public Flux<Customer> getCustomersByLastName(String lastName) {
        return customerRepository.findByLastName(lastName);
    }

    public Mono<Customer> createCustomer(Customer customer) {
        log.debug("Creating customer: {}", customer.getCustomerId());
        return customerRepository.save(customer);
    }

    public Mono<Customer> updateCustomer(String customerId, Customer customerUpdate) {
        log.debug("Updating customer: {}", customerId);
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Customer not found: " + customerId)))
                .flatMap(existing -> {
                    if (customerUpdate.getFirstName() != null) existing.setFirstName(customerUpdate.getFirstName());
                    if (customerUpdate.getMiddleName() != null) existing.setMiddleName(customerUpdate.getMiddleName());
                    if (customerUpdate.getLastName() != null) existing.setLastName(customerUpdate.getLastName());
                    if (customerUpdate.getAddressLine1() != null) existing.setAddressLine1(customerUpdate.getAddressLine1());
                    if (customerUpdate.getAddressLine2() != null) existing.setAddressLine2(customerUpdate.getAddressLine2());
                    if (customerUpdate.getAddressLine3() != null) existing.setAddressLine3(customerUpdate.getAddressLine3());
                    if (customerUpdate.getStateCode() != null) existing.setStateCode(customerUpdate.getStateCode());
                    if (customerUpdate.getCountryCode() != null) existing.setCountryCode(customerUpdate.getCountryCode());
                    if (customerUpdate.getZipCode() != null) existing.setZipCode(customerUpdate.getZipCode());
                    if (customerUpdate.getPhoneNumber1() != null) existing.setPhoneNumber1(customerUpdate.getPhoneNumber1());
                    if (customerUpdate.getPhoneNumber2() != null) existing.setPhoneNumber2(customerUpdate.getPhoneNumber2());
                    if (customerUpdate.getEftAccountId() != null) existing.setEftAccountId(customerUpdate.getEftAccountId());
                    if (customerUpdate.getFicoCreditScore() != null) existing.setFicoCreditScore(customerUpdate.getFicoCreditScore());
                    return customerRepository.save(existing);
                });
    }
}
