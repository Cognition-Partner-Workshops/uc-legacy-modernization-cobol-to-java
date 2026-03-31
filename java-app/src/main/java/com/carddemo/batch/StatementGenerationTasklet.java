package com.carddemo.batch;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Customer;
import com.carddemo.model.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class StatementGenerationTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationTasklet.class);

    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public StatementGenerationTasklet(CardXrefRepository cardXrefRepository,
                                      CustomerRepository customerRepository,
                                      AccountRepository accountRepository,
                                      TransactionRepository transactionRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<CardXref> allXrefs = cardXrefRepository.findAll();
        log.info("Generating statements for {} cards", allXrefs.size());

        int statementsGenerated = 0;

        for (CardXref xref : allXrefs) {
            try {
                Optional<Customer> customerOpt = customerRepository.findById(xref.getCustId());
                Optional<Account> accountOpt = accountRepository.findById(xref.getAcctId());
                List<Transaction> transactions = transactionRepository
                        .findByCardNumOrderByTranIdDesc(xref.getCardNum());

                if (customerOpt.isEmpty() || accountOpt.isEmpty()) {
                    continue;
                }

                Customer customer = customerOpt.get();
                Account account = accountOpt.get();

                // Generate statement text (CBSTM03A.CBL lines 262-342)
                StringBuilder statement = new StringBuilder();
                statement.append("=".repeat(80)).append("\n");
                statement.append("             CARDDEMO CREDIT CARD STATEMENT\n");
                statement.append("=".repeat(80)).append("\n\n");

                statement.append("Customer:  ").append(customer.getFirstName().trim())
                        .append(" ").append(customer.getLastName().trim()).append("\n");
                statement.append("Address:   ").append(customer.getAddressLine1().trim()).append("\n");
                if (customer.getAddressLine2() != null && !customer.getAddressLine2().isBlank()) {
                    statement.append("           ").append(customer.getAddressLine2().trim()).append("\n");
                }
                statement.append("           ").append(customer.getStateCode())
                        .append(" ").append(customer.getZipCode()).append("\n\n");

                statement.append("Account:   ").append(String.format("%011d", account.getAcctId())).append("\n");
                statement.append("Card:      ").append(xref.getCardNum()).append("\n");
                statement.append("Balance:   $").append(account.getCurrBal()).append("\n");
                statement.append("Credit Limit: $").append(account.getCreditLimit()).append("\n\n");

                statement.append("-".repeat(80)).append("\n");
                statement.append(String.format("%-16s %-2s %-10s %-30s %12s\n",
                        "TRAN ID", "TP", "DATE", "DESCRIPTION", "AMOUNT"));
                statement.append("-".repeat(80)).append("\n");

                for (Transaction t : transactions) {
                    String date = t.getOrigTimestamp() != null && t.getOrigTimestamp().length() >= 10
                            ? t.getOrigTimestamp().substring(0, 10) : "";
                    String desc = t.getDescription() != null
                            ? (t.getDescription().length() > 30
                            ? t.getDescription().substring(0, 30) : t.getDescription()) : "";
                    statement.append(String.format("%-16s %-2s %-10s %-30s %12s\n",
                            t.getTranId(), t.getTypeCd(), date, desc, t.getAmount()));
                }

                statement.append("-".repeat(80)).append("\n");
                statement.append("Total Transactions: ").append(transactions.size()).append("\n");
                statement.append("=".repeat(80)).append("\n");

                // Log the statement (in production, would write to file or send)
                log.debug("Statement for card {}: \n{}", xref.getCardNum(), statement);
                statementsGenerated++;

            } catch (Exception e) {
                log.error("Error generating statement for card {}: {}",
                        xref.getCardNum(), e.getMessage());
            }
        }

        log.info("Statement generation complete. Statements generated: {}", statementsGenerated);
        return RepeatStatus.FINISHED;
    }
}
