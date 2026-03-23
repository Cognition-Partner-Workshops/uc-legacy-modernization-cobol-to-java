package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Replaces CBSTM03A.CBL and CBSTM03B.CBL - Statement generation.
 */
@Service
public class StatementGenerationService {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationService.class);

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public StatementGenerationService(CustomerRepository customerRepository,
                                       AccountRepository accountRepository,
                                       CardXrefRepository cardXrefRepository,
                                       TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    public Map<String, Object> generateStatement(String acctId) {
        Map<String, Object> statement = new HashMap<>();

        Account account = accountRepository.findById(acctId).orElse(null);
        statement.put("account", account);

        List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);
        if (!xrefs.isEmpty()) {
            Customer customer = customerRepository.findById(xrefs.get(0).getCustId()).orElse(null);
            statement.put("customer", customer);
        }

        List<String> cardNums = xrefs.stream().map(CardXref::getCardNum).collect(Collectors.toList());
        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> cardNums.contains(t.getCardNum()))
                .collect(Collectors.toList());
        statement.put("transactions", transactions);

        log.info("Generated statement for account: {}", acctId);
        return statement;
    }
}
