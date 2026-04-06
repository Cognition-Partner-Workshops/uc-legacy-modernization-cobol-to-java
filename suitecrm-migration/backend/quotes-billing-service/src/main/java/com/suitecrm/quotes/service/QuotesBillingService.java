package com.suitecrm.quotes.service;

import com.suitecrm.quotes.dto.*;
import com.suitecrm.quotes.entity.*;
import com.suitecrm.quotes.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuotesBillingService {

    private final QuoteRepository quoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;

    public Page<QuoteDto> getAllQuotes(Pageable pageable) {
        return quoteRepository.findByDeletedFalse(pageable).map(this::toQuoteDto);
    }

    public QuoteDto getQuoteById(UUID id) {
        return toQuoteDto(quoteRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Quote not found: " + id)));
    }

    @Transactional
    public QuoteDto createQuote(QuoteCreateRequest req) {
        Quote quote = Quote.builder()
                .name(req.getName()).quoteStage(req.getQuoteStage()).paymentTerms(req.getPaymentTerms())
                .validUntil(req.getValidUntil()).subtotalAmount(req.getSubtotalAmount())
                .discountAmount(req.getDiscountAmount()).taxAmount(req.getTaxAmount())
                .shippingAmount(req.getShippingAmount()).totalAmount(req.getTotalAmount())
                .accountId(req.getAccountId()).contactId(req.getContactId())
                .opportunityId(req.getOpportunityId()).assignedUserId(req.getAssignedUserId())
                .description(req.getDescription())
                .billingAddressStreet(req.getBillingAddressStreet()).billingAddressCity(req.getBillingAddressCity())
                .billingAddressState(req.getBillingAddressState()).billingAddressPostalcode(req.getBillingAddressPostalcode())
                .billingAddressCountry(req.getBillingAddressCountry())
                .shippingAddressStreet(req.getShippingAddressStreet()).shippingAddressCity(req.getShippingAddressCity())
                .shippingAddressState(req.getShippingAddressState()).shippingAddressPostalcode(req.getShippingAddressPostalcode())
                .shippingAddressCountry(req.getShippingAddressCountry())
                .build();
        return toQuoteDto(quoteRepository.save(quote));
    }

    @Transactional
    public void deleteQuote(UUID id) {
        Quote quote = quoteRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Quote not found: " + id));
        quote.setDeleted(true);
        quoteRepository.save(quote);
    }

    public Page<InvoiceDto> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findByDeletedFalse(pageable).map(this::toInvoiceDto);
    }

    public Page<ContractDto> getAllContracts(Pageable pageable) {
        return contractRepository.findByDeletedFalse(pageable).map(this::toContractDto);
    }

    private QuoteDto toQuoteDto(Quote q) {
        return QuoteDto.builder().id(q.getId()).name(q.getName()).quoteNum(q.getQuoteNum())
                .quoteStage(q.getQuoteStage()).paymentTerms(q.getPaymentTerms())
                .validUntil(q.getValidUntil()).subtotalAmount(q.getSubtotalAmount())
                .discountAmount(q.getDiscountAmount()).taxAmount(q.getTaxAmount())
                .shippingAmount(q.getShippingAmount()).totalAmount(q.getTotalAmount())
                .accountId(q.getAccountId()).contactId(q.getContactId())
                .opportunityId(q.getOpportunityId()).assignedUserId(q.getAssignedUserId())
                .description(q.getDescription())
                .dateEntered(q.getDateEntered()).dateModified(q.getDateModified()).build();
    }

    private InvoiceDto toInvoiceDto(Invoice i) {
        return InvoiceDto.builder().id(i.getId()).name(i.getName()).invoiceNumber(i.getInvoiceNumber())
                .quoteId(i.getQuoteId()).status(i.getStatus()).dueDate(i.getDueDate())
                .subtotalAmount(i.getSubtotalAmount()).totalAmount(i.getTotalAmount())
                .accountId(i.getAccountId()).contactId(i.getContactId())
                .assignedUserId(i.getAssignedUserId()).description(i.getDescription())
                .dateEntered(i.getDateEntered()).dateModified(i.getDateModified()).build();
    }

    private ContractDto toContractDto(Contract c) {
        return ContractDto.builder().id(c.getId()).name(c.getName()).referenceCode(c.getReferenceCode())
                .status(c.getStatus()).contractType(c.getContractType())
                .startDate(c.getStartDate()).endDate(c.getEndDate())
                .totalContractValue(c.getTotalContractValue())
                .accountId(c.getAccountId()).contactId(c.getContactId())
                .opportunityId(c.getOpportunityId()).assignedUserId(c.getAssignedUserId())
                .description(c.getDescription())
                .dateEntered(c.getDateEntered()).dateModified(c.getDateModified()).build();
    }
}
