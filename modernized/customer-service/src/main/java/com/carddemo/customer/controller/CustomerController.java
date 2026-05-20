package com.carddemo.customer.controller;

import com.carddemo.common.dto.PageResponse;
import com.carddemo.customer.dto.CreateCustomerRequest;
import com.carddemo.customer.dto.CustomerDto;
import com.carddemo.customer.dto.UpdateCustomerRequest;
import com.carddemo.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CustomerDto>> listCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(customerService.listCustomers(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable String id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(
            @RequestParam(required = false) String ssn,
            @RequestParam(required = false) String lastName) {
        if (ssn != null) {
            return ResponseEntity.ok(customerService.searchBySsn(ssn));
        } else if (lastName != null) {
            return ResponseEntity.ok(customerService.searchByName(lastName));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable String id,
                                                       @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }
}
