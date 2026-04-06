package com.suitecrm.opportunity.service;

import com.suitecrm.opportunity.entity.Quote;
import com.suitecrm.opportunity.entity.LineItem;
import com.suitecrm.opportunity.entity.LineItemGroup;
import com.suitecrm.opportunity.repository.QuoteRepository;
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
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final LineItemRepository lineItemRepository;

    @Transactional(readOnly = true)
    public Page<Quote> listQuotes(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return quoteRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Quote getQuote(UUID id) {
        return quoteRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + id));
    }

    public Quote createQuote(Quote quote) {
        log.info("Creating quote: name={}", quote.getName());
        Quote saved = quoteRepository.save(quote);
        log.info("Created quote: id={}", saved.getId());
        return saved;
    }

    public Quote updateQuote(UUID id, Quote quoteUpdate) {
        Quote existing = quoteRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + id));
        existing.setName(quoteUpdate.getName());
        existing.setQuoteStage(quoteUpdate.getQuoteStage());
        existing.setValidUntil(quoteUpdate.getValidUntil());
        existing.setPaymentTerms(quoteUpdate.getPaymentTerms());
        existing.setSubtotal(quoteUpdate.getSubtotal());
        existing.setDiscountAmount(quoteUpdate.getDiscountAmount());
        existing.setTaxAmount(quoteUpdate.getTaxAmount());
        existing.setShippingAmount(quoteUpdate.getShippingAmount());
        existing.setTotal(quoteUpdate.getTotal());
        existing.setGrandTotal(quoteUpdate.getGrandTotal());
        existing.setDescription(quoteUpdate.getDescription());
        return quoteRepository.save(existing);
    }

    public void deleteQuote(UUID id) {
        Quote quote = quoteRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Quote not found with id: " + id));
        quote.setDeleted(true);
        quoteRepository.save(quote);
    }

    @Transactional(readOnly = true)
    public List<LineItem> getQuoteLineItems(UUID quoteId) {
        return lineItemRepository.findByParentIdAndParentTypeAndDeletedFalse(quoteId, "Quotes");
    }

    @Transactional(readOnly = true)
    public Page<Quote> getQuotesByAccount(UUID accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateEntered").descending());
        return quoteRepository.findByAccountIdAndDeletedFalse(accountId, pageable);
    }
}
