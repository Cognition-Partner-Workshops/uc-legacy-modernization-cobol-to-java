package com.carddemo.service;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Customer;
import com.carddemo.model.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public ReportService(TransactionRepository transactionRepository,
                         CardXrefRepository cardXrefRepository,
                         AccountRepository accountRepository,
                         CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Map<String, Object> generateTransactionReport(Long acctId, String cardNum,
                                                          String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        List<Transaction> transactions;

        if (cardNum != null && !cardNum.isBlank()) {
            transactions = transactionRepository.findByCardNumOrderByTranIdDesc(cardNum);
        } else {
            transactions = transactionRepository.findAll();
        }

        // Filter by date range if provided
        if (startDate != null && !startDate.isBlank()) {
            transactions = transactions.stream()
                    .filter(t -> t.getOrigTimestamp() != null &&
                            t.getOrigTimestamp().compareTo(startDate) >= 0)
                    .toList();
        }
        if (endDate != null && !endDate.isBlank()) {
            transactions = transactions.stream()
                    .filter(t -> t.getOrigTimestamp() != null &&
                            t.getOrigTimestamp().compareTo(endDate + "Z") <= 0)
                    .toList();
        }

        // If acctId is specified, filter by account
        if (acctId != null && acctId > 0) {
            List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);
            List<String> cardNums = xrefs.stream().map(CardXref::getCardNum).toList();
            transactions = transactions.stream()
                    .filter(t -> cardNums.contains(t.getCardNum()))
                    .toList();

            Optional<Account> account = accountRepository.findById(acctId);
            account.ifPresent(a -> report.put("account", a));

            if (!xrefs.isEmpty()) {
                Optional<Customer> customer = customerRepository.findById(xrefs.get(0).getCustId());
                customer.ifPresent(c -> report.put("customer", c));
            }
        }

        report.put("transactions", transactions);
        report.put("totalCount", transactions.size());
        report.put("totalAmount", transactions.stream()
                .map(Transaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));

        return report;
    }

    public String generateStatementText(Long acctId) {
        Map<String, Object> reportData = generateTransactionReport(acctId, null, null, null);
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(80)).append("\n");
        sb.append("                    CARDDEMO TRANSACTION STATEMENT\n");
        sb.append("=".repeat(80)).append("\n\n");

        Account account = (Account) reportData.get("account");
        Customer customer = (Customer) reportData.get("customer");

        if (customer != null) {
            sb.append("Customer: ").append(customer.getFirstName()).append(" ")
                    .append(customer.getLastName()).append("\n");
            sb.append("Address:  ").append(customer.getAddressLine1()).append("\n");
            if (customer.getAddressLine2() != null && !customer.getAddressLine2().isBlank()) {
                sb.append("          ").append(customer.getAddressLine2()).append("\n");
            }
            sb.append("          ").append(customer.getStateCode()).append(" ")
                    .append(customer.getZipCode()).append("\n\n");
        }

        if (account != null) {
            sb.append("Account ID:    ").append(String.format("%011d", account.getAcctId())).append("\n");
            sb.append("Current Bal:   ").append(account.getCurrBal()).append("\n");
            sb.append("Credit Limit:  ").append(account.getCreditLimit()).append("\n\n");
        }

        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("%-16s %-2s %-10s %-30s %12s\n",
                "TRAN ID", "TP", "DATE", "DESCRIPTION", "AMOUNT"));
        sb.append("-".repeat(80)).append("\n");

        @SuppressWarnings("unchecked")
        List<Transaction> transactions = (List<Transaction>) reportData.get("transactions");
        if (transactions != null) {
            for (Transaction t : transactions) {
                String date = t.getOrigTimestamp() != null && t.getOrigTimestamp().length() >= 10
                        ? t.getOrigTimestamp().substring(0, 10) : "";
                sb.append(String.format("%-16s %-2s %-10s %-30s %12s\n",
                        t.getTranId(), t.getTypeCd(), date,
                        t.getDescription() != null ?
                                (t.getDescription().length() > 30 ?
                                        t.getDescription().substring(0, 30) : t.getDescription()) : "",
                        t.getAmount()));
            }
        }

        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("Total Transactions: %d   Total Amount: %s\n",
                reportData.get("totalCount"), reportData.get("totalAmount")));
        sb.append("=".repeat(80)).append("\n");

        return sb.toString();
    }
}
