package com.suitecrm.opportunity.controller;

import com.suitecrm.opportunity.entity.Quote;
import com.suitecrm.opportunity.entity.LineItem;
import com.suitecrm.opportunity.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER','VIEWER')")
    public ResponseEntity<Page<Quote>> listQuotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(quoteService.listQuotes(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER','VIEWER')")
    public ResponseEntity<Quote> getQuote(@PathVariable UUID id) {
        return ResponseEntity.ok(quoteService.getQuote(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER')")
    public ResponseEntity<Quote> createQuote(@RequestBody Quote quote) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteService.createQuote(quote));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER')")
    public ResponseEntity<Quote> updateQuote(@PathVariable UUID id, @RequestBody Quote quote) {
        return ResponseEntity.ok(quoteService.updateQuote(id, quote));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteQuote(@PathVariable UUID id) {
        quoteService.deleteQuote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/line-items")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER','VIEWER')")
    public ResponseEntity<List<LineItem>> getQuoteLineItems(@PathVariable UUID id) {
        return ResponseEntity.ok(quoteService.getQuoteLineItems(id));
    }
}
