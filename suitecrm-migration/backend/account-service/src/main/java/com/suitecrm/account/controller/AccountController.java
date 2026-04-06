package com.suitecrm.account.controller;

import com.suitecrm.account.entity.Account;
import com.suitecrm.account.repository.AccountRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Page<Account>> listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Account> accounts;
        if (search != null && !search.isBlank()) {
            accounts = accountRepository.searchAccounts(search, pageable);
        } else {
            accounts = accountRepository.findByDeletedFalse(pageable);
        }
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Account> getAccount(@PathVariable UUID id) {
        Account account = accountRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return ResponseEntity.ok(account);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER')")
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account account) {
        Account saved = accountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','USER')")
    public ResponseEntity<Account> updateAccount(@PathVariable UUID id, @Valid @RequestBody Account accountUpdate) {
        Account existing = accountRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        existing.setName(accountUpdate.getName());
        existing.setAccountType(accountUpdate.getAccountType());
        existing.setIndustry(accountUpdate.getIndustry());
        existing.setAnnualRevenue(accountUpdate.getAnnualRevenue());
        existing.setPhoneOffice(accountUpdate.getPhoneOffice());
        existing.setWebsite(accountUpdate.getWebsite());
        existing.setEmail(accountUpdate.getEmail());
        existing.setBillingAddressStreet(accountUpdate.getBillingAddressStreet());
        existing.setBillingAddressCity(accountUpdate.getBillingAddressCity());
        existing.setBillingAddressState(accountUpdate.getBillingAddressState());
        existing.setBillingAddressPostalcode(accountUpdate.getBillingAddressPostalcode());
        existing.setBillingAddressCountry(accountUpdate.getBillingAddressCountry());
        existing.setDescription(accountUpdate.getDescription());

        Account saved = accountRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        Account account = accountRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setDeleted(true);
        accountRepository.save(account);
        return ResponseEntity.noContent().build();
    }
}
