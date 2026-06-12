package com.carddemo.customer.controller;

import com.carddemo.customer.dto.AccountXrefDto;
import com.carddemo.customer.dto.CustomerDto;
import com.carddemo.customer.entity.CardXref;
import com.carddemo.customer.entity.Customer;
import com.carddemo.customer.repository.CardXrefRepository;
import com.carddemo.customer.repository.CustomerRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CardXrefRepository cardXrefRepository;

    public CustomerController(CustomerRepository customerRepository,
                              CardXrefRepository cardXrefRepository) {
        this.customerRepository = customerRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    @GetMapping
    public Page<CustomerDto> listCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(CustomerDto::fromEntity);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(CustomerDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/accounts")
    public ResponseEntity<List<AccountXrefDto>> getCustomerAccounts(@PathVariable Long id) {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<CardXref> xrefs = cardXrefRepository.findByCustomerId(id);
        List<AccountXrefDto> accounts = xrefs.stream()
                .map(xref -> new AccountXrefDto(xref.getAccountId(), xref.getCardNumber()))
                .toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/search")
    public Page<CustomerDto> searchCustomers(
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String zip,
            Pageable pageable) {
        return customerRepository.search(lastName, zip, pageable)
                .map(CustomerDto::fromEntity);
    }
}
