package com.suitecrm.contact.service;

import com.suitecrm.contact.dto.*;
import com.suitecrm.contact.entity.Contact;
import com.suitecrm.contact.mapper.ContactMapper;
import com.suitecrm.contact.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    @Transactional(readOnly = true)
    public Page<ContactDto> listContacts(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Contact> contacts;
        if (search != null && !search.isBlank()) {
            contacts = contactRepository.searchContacts(search, pageable);
        } else {
            contacts = contactRepository.findByDeletedFalse(pageable);
        }
        return contacts.map(contactMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ContactDto getContact(UUID id) {
        Contact contact = contactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));
        return contactMapper.toDto(contact);
    }

    public ContactDto createContact(ContactCreateRequest request, UUID createdBy) {
        log.info("Creating contact: {} {}", request.getFirstName(), request.getLastName());
        Contact contact = contactMapper.toEntity(request);
        contact.setCreatedBy(createdBy);
        Contact saved = contactRepository.save(contact);
        log.info("Created contact: id={}", saved.getId());
        return contactMapper.toDto(saved);
    }

    public ContactDto updateContact(UUID id, ContactCreateRequest request) {
        log.info("Updating contact: id={}", id);
        Contact existing = contactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));
        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setSalutation(request.getSalutation());
        existing.setTitle(request.getTitle());
        existing.setDepartment(request.getDepartment());
        existing.setPhoneWork(request.getPhoneWork());
        existing.setPhoneMobile(request.getPhoneMobile());
        existing.setPhoneHome(request.getPhoneHome());
        existing.setPhoneFax(request.getPhoneFax());
        existing.setEmail(request.getEmail());
        existing.setPrimaryAddressStreet(request.getPrimaryAddressStreet());
        existing.setPrimaryAddressCity(request.getPrimaryAddressCity());
        existing.setPrimaryAddressState(request.getPrimaryAddressState());
        existing.setPrimaryAddressPostalcode(request.getPrimaryAddressPostalcode());
        existing.setPrimaryAddressCountry(request.getPrimaryAddressCountry());
        existing.setAltAddressStreet(request.getAltAddressStreet());
        existing.setAltAddressCity(request.getAltAddressCity());
        existing.setAltAddressState(request.getAltAddressState());
        existing.setAltAddressPostalcode(request.getAltAddressPostalcode());
        existing.setAltAddressCountry(request.getAltAddressCountry());
        existing.setDescription(request.getDescription());
        existing.setAccountId(request.getAccountId());
        existing.setReportsToId(request.getReportsToId());
        existing.setLeadSource(request.getLeadSource());
        existing.setAssignedUserId(request.getAssignedUserId());
        Contact saved = contactRepository.save(existing);
        return contactMapper.toDto(saved);
    }

    public void deleteContact(UUID id) {
        log.info("Soft-deleting contact: id={}", id);
        Contact contact = contactRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));
        contact.setDeleted(true);
        contactRepository.save(contact);
    }

    @Transactional(readOnly = true)
    public Page<ContactDto> getContactsByAccount(UUID accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
        return contactRepository.findByAccountIdAndDeletedFalse(accountId, pageable)
                .map(contactMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countContacts() {
        return contactRepository.countByDeletedFalse();
    }
}
