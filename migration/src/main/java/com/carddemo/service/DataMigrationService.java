package com.carddemo.service;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replaces CBEXPORT.cbl and CBIMPORT.cbl - Data export/import functionality.
 */
@Service
public class DataMigrationService {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationService.class);

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;
    private final UserSecurityRepository userSecurityRepository;
    private final TransactionCategoryBalanceRepository tcatBalRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    public DataMigrationService(CustomerRepository customerRepository,
                                 AccountRepository accountRepository,
                                 CardRepository cardRepository,
                                 CardXrefRepository cardXrefRepository,
                                 TransactionRepository transactionRepository,
                                 UserSecurityRepository userSecurityRepository,
                                 TransactionCategoryBalanceRepository tcatBalRepository,
                                 DisclosureGroupRepository disclosureGroupRepository,
                                 TransactionTypeRepository transactionTypeRepository,
                                 TransactionCategoryRepository transactionCategoryRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
        this.userSecurityRepository = userSecurityRepository;
        this.tcatBalRepository = tcatBalRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
    }

    /**
     * Export all data entities to a map structure.
     */
    public Map<String, Object> exportAll() {
        Map<String, Object> data = new HashMap<>();
        data.put("customers", customerRepository.findAll());
        data.put("accounts", accountRepository.findAll());
        data.put("cards", cardRepository.findAll());
        data.put("cardXrefs", cardXrefRepository.findAll());
        data.put("transactions", transactionRepository.findAll());
        data.put("users", userSecurityRepository.findAll());
        data.put("categoryBalances", tcatBalRepository.findAll());
        data.put("disclosureGroups", disclosureGroupRepository.findAll());
        data.put("transactionTypes", transactionTypeRepository.findAll());
        data.put("transactionCategories", transactionCategoryRepository.findAll());

        log.info("Data export completed");
        return data;
    }

    /**
     * Import data from entity lists.
     */
    @Transactional
    public void importData(List<Customer> customers, List<Account> accounts,
                           List<Card> cards, List<CardXref> xrefs) {
        if (customers != null) customerRepository.saveAll(customers);
        if (accounts != null) accountRepository.saveAll(accounts);
        if (cards != null) cardRepository.saveAll(cards);
        if (xrefs != null) cardXrefRepository.saveAll(xrefs);

        log.info("Data import completed");
    }
}
