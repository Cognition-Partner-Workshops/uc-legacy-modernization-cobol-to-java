package com.suitecrm.opportunity.service;

import com.suitecrm.opportunity.entity.Invoice;
import com.suitecrm.opportunity.entity.LineItem;
import com.suitecrm.opportunity.repository.InvoiceRepository;
import com.suitecrm.opportunity.repository.LineItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final LineItemRepository lineItemRepository;

    @Transactional(readOnly = true)
    public Page<Invoice> listInvoices(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return invoiceRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoice(UUID id) {
        return invoiceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    public Invoice createInvoice(Invoice invoice) {
        log.info("Creating invoice: name={}", invoice.getName());
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created invoice: id={}, number={}", saved.getId(), saved.getInvoiceNumber());
        return saved;
    }

    public void deleteInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
        invoice.setDeleted(true);
        invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public List<LineItem> getInvoiceLineItems(UUID invoiceId) {
        return lineItemRepository.findByParentIdAndParentTypeAndDeletedFalse(invoiceId, "Invoices");
    }

    @Transactional(readOnly = true)
    public Page<Invoice> getInvoicesByAccount(UUID accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("invoiceDate").descending());
        return invoiceRepository.findByAccountIdAndDeletedFalse(accountId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Invoice> getInvoicesByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dueDate").ascending());
        return invoiceRepository.findByStatusAndDeletedFalse(status, pageable);
    }
}
