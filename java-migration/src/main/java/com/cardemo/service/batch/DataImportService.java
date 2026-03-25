package com.cardemo.service.batch;

import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.CustomerRecord;
import com.cardemo.model.TransactionRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Import Service - migrated from COBOL batch program CBIMPORT.cbl.
 * Imports customer data from branch migration export.
 * Reads multi-record export data and splits into normalized target tables.
 * Original: Reads EXPORT-INPUT; writes to CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT,
 *           CARDOUT, ERROUT based on record type (C, A, X, T, D).
 */
@Service
public class DataImportService {

    private static final Logger log = LoggerFactory.getLogger(DataImportService.class);

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public DataImportService(CustomerRepository customerRepository,
                             AccountRepository accountRepository,
                             CardXrefRepository cardXrefRepository,
                             TransactionRepository transactionRepository,
                             CardRepository cardRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
    }

    /**
     * Import customers from external data.
     */
    @Transactional
    public int importCustomers(List<CustomerRecord> customers) {
        log.info("CBIMPORT: Processing customer records");
        List<CustomerRecord> saved = customerRepository.saveAll(customers);
        log.info("CBIMPORT: Customers imported: {}", saved.size());
        return saved.size();
    }

    /**
     * Import accounts from external data.
     */
    @Transactional
    public int importAccounts(List<AccountRecord> accounts) {
        log.info("CBIMPORT: Processing account records");
        List<AccountRecord> saved = accountRepository.saveAll(accounts);
        log.info("CBIMPORT: Accounts imported: {}", saved.size());
        return saved.size();
    }

    /**
     * Import card cross-references from external data.
     */
    @Transactional
    public int importXrefs(List<CardXrefRecord> xrefs) {
        log.info("CBIMPORT: Processing cross-reference records");
        List<CardXrefRecord> saved = cardXrefRepository.saveAll(xrefs);
        log.info("CBIMPORT: Cross-references imported: {}", saved.size());
        return saved.size();
    }

    /**
     * Import transactions from external data.
     */
    @Transactional
    public int importTransactions(List<TransactionRecord> transactions) {
        log.info("CBIMPORT: Processing transaction records");
        List<TransactionRecord> saved = transactionRepository.saveAll(transactions);
        log.info("CBIMPORT: Transactions imported: {}", saved.size());
        return saved.size();
    }

    /**
     * Import cards from external data.
     */
    @Transactional
    public int importCards(List<CardRecord> cards) {
        log.info("CBIMPORT: Processing card records");
        List<CardRecord> saved = cardRepository.saveAll(cards);
        log.info("CBIMPORT: Cards imported: {}", saved.size());
        return saved.size();
    }

    /**
     * Get import statistics.
     */
    public Map<String, Long> getImportStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("customers", customerRepository.count());
        stats.put("accounts", accountRepository.count());
        stats.put("xrefs", cardXrefRepository.count());
        stats.put("transactions", transactionRepository.count());
        stats.put("cards", cardRepository.count());
        return stats;
    }
}
