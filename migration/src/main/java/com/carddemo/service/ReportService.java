package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Replaces CORPT00C.cbl (report screen) and CBTRN03C.cbl (transaction report).
 */
@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;

    public ReportService(TransactionRepository transactionRepository,
                         AccountRepository accountRepository,
                         CardXrefRepository cardXrefRepository,
                         CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
    }

    public Map<String, Object> generateTransactionReport(String acctId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();

        Account account = accountRepository.findById(acctId).orElse(null);
        report.put("account", account);

        List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);
        List<String> cardNums = xrefs.stream().map(CardXref::getCardNum).collect(Collectors.toList());
        report.put("cardNumbers", cardNums);

        if (!xrefs.isEmpty()) {
            Customer customer = customerRepository.findById(xrefs.get(0).getCustId()).orElse(null);
            report.put("customer", customer);
        }

        List<Transaction> allTransactions = transactionRepository.findAll();
        List<Transaction> filtered = allTransactions.stream()
                .filter(t -> cardNums.contains(t.getCardNum()))
                .filter(t -> {
                    if (t.getOrigTimestamp() == null) return false;
                    LocalDate tranDate = t.getOrigTimestamp().toLocalDate();
                    boolean afterStart = startDate == null || !tranDate.isBefore(startDate);
                    boolean beforeEnd = endDate == null || !tranDate.isAfter(endDate);
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());

        report.put("transactions", filtered);
        report.put("transactionCount", filtered.size());
        report.put("startDate", startDate);
        report.put("endDate", endDate);

        return report;
    }
}
