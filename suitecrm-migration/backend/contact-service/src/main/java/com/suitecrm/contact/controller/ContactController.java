package com.suitecrm.contact.controller;

import com.suitecrm.contact.entity.Contact;
import com.suitecrm.contact.repository.ContactRepository;
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
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactRepository contactRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','USER','VIEWER')")
    public ResponseEntity<Page<Contact>> listContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID accountId) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Contact> contacts;
        if (search != null && !search.isBlank()) {
            contacts = contactRepository.searchContacts(search, pageable);
        } else if (accountId != null) {
            contacts = contactRepository.findByAccountIdAndDeletedFalse(accountId, pageable);
        } else {
            contacts = contactRepository.findByDeletedFalse(pageable);
        }
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','USER','VIEWER')")
    public ResponseEntity<Contact> getContact(@PathVariable UUID id) {
        Contact contact = contactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        return ResponseEntity.ok(contact);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','USER')")
    public ResponseEntity<Contact> createContact(@Valid @RequestBody Contact contact) {
        Contact saved = contactRepository.save(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','USER')")
    public ResponseEntity<Contact> updateContact(@PathVariable UUID id, @Valid @RequestBody Contact contactUpdate) {
        Contact existing = contactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        existing.setSalutation(contactUpdate.getSalutation());
        existing.setFirstName(contactUpdate.getFirstName());
        existing.setLastName(contactUpdate.getLastName());
        existing.setTitle(contactUpdate.getTitle());
        existing.setDepartment(contactUpdate.getDepartment());
        existing.setAccountId(contactUpdate.getAccountId());
        existing.setEmailPrimary(contactUpdate.getEmailPrimary());
        existing.setEmailSecondary(contactUpdate.getEmailSecondary());
        existing.setPhoneWork(contactUpdate.getPhoneWork());
        existing.setPhoneMobile(contactUpdate.getPhoneMobile());
        existing.setPrimaryAddressStreet(contactUpdate.getPrimaryAddressStreet());
        existing.setPrimaryAddressCity(contactUpdate.getPrimaryAddressCity());
        existing.setPrimaryAddressState(contactUpdate.getPrimaryAddressState());
        existing.setPrimaryAddressPostalcode(contactUpdate.getPrimaryAddressPostalcode());
        existing.setPrimaryAddressCountry(contactUpdate.getPrimaryAddressCountry());
        existing.setDescription(contactUpdate.getDescription());
        existing.setLeadSource(contactUpdate.getLeadSource());
        existing.setDoNotCall(contactUpdate.getDoNotCall());

        Contact saved = contactRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteContact(@PathVariable UUID id) {
        Contact contact = contactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
        contact.setDeleted(true);
        contactRepository.save(contact);
        return ResponseEntity.noContent().build();
    }
}
