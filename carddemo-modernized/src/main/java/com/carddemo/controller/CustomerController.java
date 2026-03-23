package com.carddemo.controller;

import com.carddemo.model.Customer;
import com.carddemo.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Customer controller - replaces COBOL customer data operations.
 *
 * Legacy: Customer data accessed via VSAM KSDS (CUSTDAT) in programs
 *         COACTVWC (account view reads customer), CBCUS01C (batch processing)
 *
 * Modernized: Reactive REST endpoints with Cassandra backend
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public Flux<Customer> listCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{customerId}")
    public Mono<Customer> viewCustomer(@PathVariable String customerId) {
        return customerService.getCustomerById(customerId);
    }

    @GetMapping("/search")
    public Flux<Customer> searchByLastName(@RequestParam String lastName) {
        return customerService.getCustomersByLastName(lastName);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        return customerService.createCustomer(customer);
    }

    @PutMapping("/{customerId}")
    public Mono<Customer> updateCustomer(@PathVariable String customerId,
                                         @Valid @RequestBody Customer customerUpdate) {
        return customerService.updateCustomer(customerId, customerUpdate);
    }
}
