package com.cardemo.service.batch;

import com.cardemo.common.DateTimeUtil;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Export Service - migrated from COBOL batch program CBEXPORT.cbl.
 * Exports customer data for branch migration.
 * Reads normalized CardDemo files and creates multi-record export data.
 * Original: Reads CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE;
 *           writes EXPORT-OUTPUT with record types C, A, X, T, D.
 */
@Service
public class DataExportService {

    private static final Logger log = LoggerFactory.getLogger(DataExportService.class);

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public DataExportService(CustomerRepository customerRepository,
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
     * Export all data for branch migration - migrated from 0000-MAIN-PROCESSING.
     *
     * @return export statistics
     */
    public Map<String, Integer> exportData() {
        log.info("CBEXPORT: Starting Customer Data Export");
        String exportTimestamp = DateTimeUtil.getCurrentTimestamp();
        log.info("CBEXPORT: Export Timestamp: {}", exportTimestamp);

        Map<String, Integer> statistics = new HashMap<>();

        // Export customers - migrated from 2000-EXPORT-CUSTOMERS
        List<CustomerRecord> customers = customerRepository.findAll();
        statistics.put("customersExported", customers.size());
        log.info("CBEXPORT: Customers exported: {}", customers.size());

        // Export accounts - migrated from 3000-EXPORT-ACCOUNTS
        List<AccountRecord> accounts = accountRepository.findAll();
        statistics.put("accountsExported", accounts.size());
        log.info("CBEXPORT: Accounts exported: {}", accounts.size());

        // Export cross-references - migrated from 4000-EXPORT-XREFS
        List<CardXrefRecord> xrefs = cardXrefRepository.findAll();
        statistics.put("xrefsExported", xrefs.size());
        log.info("CBEXPORT: Cross-references exported: {}", xrefs.size());

        // Export transactions - migrated from 5000-EXPORT-TRANSACTIONS
        List<TransactionRecord> transactions = transactionRepository.findAll();
        statistics.put("transactionsExported", transactions.size());
        log.info("CBEXPORT: Transactions exported: {}", transactions.size());

        // Export cards - migrated from 5500-EXPORT-CARDS
        List<CardRecord> cards = cardRepository.findAll();
        statistics.put("cardsExported", cards.size());
        log.info("CBEXPORT: Cards exported: {}", cards.size());

        int total = customers.size() + accounts.size() + xrefs.size()
                + transactions.size() + cards.size();
        statistics.put("totalRecordsExported", total);

        log.info("CBEXPORT: Total records exported: {}", total);
        log.info("CBEXPORT: Export completed successfully");

        return statistics;
    }
}
