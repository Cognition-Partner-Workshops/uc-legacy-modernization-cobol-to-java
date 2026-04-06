package com.suitecrm.quotes.controller;

import com.suitecrm.quotes.dto.*;
import com.suitecrm.quotes.service.QuotesBillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuotesBillingController {

    private final QuotesBillingService service;

    @GetMapping("/quotes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP')")
    public ResponseEntity<Page<QuoteDto>> getAllQuotes(Pageable pageable) {
        return ResponseEntity.ok(service.getAllQuotes(pageable));
    }

    @GetMapping("/quotes/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP')")
    public ResponseEntity<QuoteDto> getQuoteById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getQuoteById(id));
    }

    @PostMapping("/quotes")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP')")
    public ResponseEntity<QuoteDto> createQuote(@Valid @RequestBody QuoteCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createQuote(request));
    }

    @DeleteMapping("/quotes/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER')")
    public ResponseEntity<Void> deleteQuote(@PathVariable UUID id) {
        service.deleteQuote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP','ROLE_FINANCE')")
    public ResponseEntity<Page<InvoiceDto>> getAllInvoices(Pageable pageable) {
        return ResponseEntity.ok(service.getAllInvoices(pageable));
    }

    @GetMapping("/contracts")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP')")
    public ResponseEntity<Page<ContractDto>> getAllContracts(Pageable pageable) {
        return ResponseEntity.ok(service.getAllContracts(pageable));
    }
}
