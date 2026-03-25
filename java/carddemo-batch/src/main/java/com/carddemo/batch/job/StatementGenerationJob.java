package com.carddemo.batch.job;

import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardCrossReferenceRepository;
import com.carddemo.batch.repository.CustomerRepository;
import com.carddemo.batch.repository.TransactionRepository;
import com.carddemo.common.model.Account;
import com.carddemo.common.model.CardCrossReference;
import com.carddemo.common.model.Customer;
import com.carddemo.common.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Batch job replacing COBOL program CBSTM03A / JCL job CREASTMT.
 * Generates account statements in plain text and HTML formats.
 *
 * Original COBOL: app/cbl/CBSTM03A.CBL
 * JCL Job: CREASTMT
 *
 * Processing logic (from COBOL):
 * 1. Read transactions ordered by card number
 * 2. Group transactions by account (via card cross-reference)
 * 3. For each account, generate a statement with:
 *    - Account holder information (from customer record)
 *    - Account summary (balance, credit limit, etc.)
 *    - Transaction details
 *    - Statement totals
 * 4. Output both plain text and HTML formatted statements
 */
@Configuration
public class StatementGenerationJob {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationJob.class);
    private static final DateTimeFormatter STMT_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${carddemo.statement.output-dir:./statements}")
    private String outputDir;

    private final TransactionRepository transactionRepository;
    private final CardCrossReferenceRepository cardCrossReferenceRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public StatementGenerationJob(
            TransactionRepository transactionRepository,
            CardCrossReferenceRepository cardCrossReferenceRepository,
            AccountRepository accountRepository,
            CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.cardCrossReferenceRepository = cardCrossReferenceRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Bean
    public Job statementGenerationBatchJob(JobRepository jobRepository,
                                           Step statementGenerationStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(statementGenerationStep)
                .build();
    }

    @Bean
    public Step statementGenerationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("statementGenerationStep", jobRepository)
                .tasklet(new StatementGenerationTasklet(), transactionManager)
                .build();
    }

    private class StatementGenerationTasklet implements Tasklet {

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            log.info("START OF EXECUTION OF PROGRAM CBSTM03A (StatementGenerationJob)");

            List<Transaction> allTransactions =
                    transactionRepository.findAllByOrderByCardNumberAscTransactionIdAsc();

            // Group transactions by card number
            Map<String, List<Transaction>> transactionsByCard = allTransactions.stream()
                    .collect(Collectors.groupingBy(
                            t -> t.getCardNumber() != null ? t.getCardNumber() : "UNKNOWN",
                            LinkedHashMap::new,
                            Collectors.toList()));

            int statementsGenerated = 0;
            StringBuilder textOutput = new StringBuilder();
            StringBuilder htmlOutput = new StringBuilder();

            htmlOutput.append("<!DOCTYPE html>\n<html>\n<head>\n");
            htmlOutput.append("<title>CardDemo Account Statements</title>\n");
            htmlOutput.append("<style>body{font-family:monospace;} table{border-collapse:collapse;width:100%;} ");
            htmlOutput.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;} ");
            htmlOutput.append("th{background-color:#4472C4;color:white;} ");
            htmlOutput.append(".total{font-weight:bold;}</style>\n");
            htmlOutput.append("</head>\n<body>\n");

            for (Map.Entry<String, List<Transaction>> entry : transactionsByCard.entrySet()) {
                String cardNumber = entry.getKey();
                List<Transaction> transactions = entry.getValue();

                Optional<CardCrossReference> xrefOpt =
                        cardCrossReferenceRepository.findById(cardNumber);
                if (xrefOpt.isEmpty()) {
                    log.warn("No cross-reference found for card: {}", cardNumber);
                    continue;
                }
                CardCrossReference xref = xrefOpt.get();

                Optional<Account> accountOpt = accountRepository.findById(xref.getAccountId());
                if (accountOpt.isEmpty()) {
                    log.warn("No account found: {}", xref.getAccountId());
                    continue;
                }
                Account account = accountOpt.get();

                Optional<Customer> customerOpt = customerRepository.findById(xref.getCustomerId());

                statementsGenerated++;

                // Generate plain text statement
                generateTextStatement(textOutput, account, customerOpt.orElse(null),
                        cardNumber, transactions);

                // Generate HTML statement
                generateHtmlStatement(htmlOutput, account, customerOpt.orElse(null),
                        cardNumber, transactions);
            }

            htmlOutput.append("</body>\n</html>\n");

            log.info("STATEMENTS GENERATED: {}", statementsGenerated);
            log.info("--- Plain Text Output ---\n{}", textOutput);
            log.info("END OF EXECUTION OF PROGRAM CBSTM03A (StatementGenerationJob)");
            return RepeatStatus.FINISHED;
        }
    }

    private void generateTextStatement(StringBuilder sb, Account account,
                                        Customer customer, String cardNumber,
                                        List<Transaction> transactions) {
        sb.append("================================================================\n");
        sb.append("                    ACCOUNT STATEMENT\n");
        sb.append("================================================================\n");
        sb.append(String.format("Statement Date: %s%n", LocalDateTime.now().format(STMT_DATE_FMT)));
        sb.append(String.format("Account ID:     %d%n", account.getAccountId()));
        sb.append(String.format("Card Number:    %s%n", cardNumber));

        if (customer != null) {
            sb.append(String.format("Account Holder: %s %s%n",
                    customer.getFirstName() != null ? customer.getFirstName().trim() : "",
                    customer.getLastName() != null ? customer.getLastName().trim() : ""));
        }

        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format("Credit Limit:        %15s%n", formatAmount(account.getCreditLimit())));
        sb.append(String.format("Current Balance:     %15s%n", formatAmount(account.getCurrentBalance())));
        sb.append(String.format("Available Credit:    %15s%n",
                formatAmount(account.getCreditLimit() != null && account.getCurrentBalance() != null
                        ? account.getCreditLimit().subtract(account.getCurrentBalance())
                        : BigDecimal.ZERO)));
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format("%-16s %-2s %-4s %-10s %15s%n",
                "Transaction ID", "Tp", "Cat", "Source", "Amount"));
        sb.append("----------------------------------------------------------------\n");

        BigDecimal statementTotal = BigDecimal.ZERO;
        for (Transaction tran : transactions) {
            BigDecimal amt = tran.getAmount() != null ? tran.getAmount() : BigDecimal.ZERO;
            statementTotal = statementTotal.add(amt);
            sb.append(String.format("%-16s %-2s %-4s %-10s %15s%n",
                    tran.getTransactionId() != null ? tran.getTransactionId() : "",
                    tran.getTypeCode() != null ? tran.getTypeCode() : "",
                    tran.getCategoryCode() != null ? tran.getCategoryCode() : "",
                    tran.getSource() != null ? tran.getSource() : "",
                    formatAmount(amt)));
        }

        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format("Statement Total: %32s%n", formatAmount(statementTotal)));
        sb.append("================================================================\n\n");
    }

    private void generateHtmlStatement(StringBuilder sb, Account account,
                                        Customer customer, String cardNumber,
                                        List<Transaction> transactions) {
        sb.append("<div style='page-break-before:always;margin:20px;'>\n");
        sb.append("<h2>Account Statement</h2>\n");
        sb.append(String.format("<p><strong>Statement Date:</strong> %s</p>%n",
                LocalDateTime.now().format(STMT_DATE_FMT)));
        sb.append(String.format("<p><strong>Account ID:</strong> %d</p>%n", account.getAccountId()));
        sb.append(String.format("<p><strong>Card Number:</strong> %s</p>%n", cardNumber));

        if (customer != null) {
            sb.append(String.format("<p><strong>Account Holder:</strong> %s %s</p>%n",
                    customer.getFirstName() != null ? customer.getFirstName().trim() : "",
                    customer.getLastName() != null ? customer.getLastName().trim() : ""));
        }

        sb.append("<h3>Account Summary</h3>\n<table>\n");
        sb.append(String.format("<tr><td>Credit Limit</td><td>%s</td></tr>%n",
                formatAmount(account.getCreditLimit())));
        sb.append(String.format("<tr><td>Current Balance</td><td>%s</td></tr>%n",
                formatAmount(account.getCurrentBalance())));
        sb.append("</table>\n");

        sb.append("<h3>Transactions</h3>\n<table>\n");
        sb.append("<tr><th>Transaction ID</th><th>Type</th><th>Category</th>");
        sb.append("<th>Source</th><th>Description</th><th>Amount</th></tr>\n");

        BigDecimal statementTotal = BigDecimal.ZERO;
        for (Transaction tran : transactions) {
            BigDecimal amt = tran.getAmount() != null ? tran.getAmount() : BigDecimal.ZERO;
            statementTotal = statementTotal.add(amt);
            sb.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>%n",
                    tran.getTransactionId() != null ? tran.getTransactionId() : "",
                    tran.getTypeCode() != null ? tran.getTypeCode() : "",
                    tran.getCategoryCode() != null ? tran.getCategoryCode() : "",
                    tran.getSource() != null ? tran.getSource() : "",
                    tran.getDescription() != null ? tran.getDescription() : "",
                    formatAmount(amt)));
        }

        sb.append(String.format("<tr class='total'><td colspan='5'>Statement Total</td><td>%s</td></tr>%n",
                formatAmount(statementTotal)));
        sb.append("</table>\n</div>\n");
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}
