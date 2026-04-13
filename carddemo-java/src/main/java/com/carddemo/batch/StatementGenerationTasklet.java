package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Statement Generation - from CBSTM03A.CBL (text) + CBSTM03B.CBL (HTML) + CREASTMT.JCL
 * Read accounts, read transactions for each account, generate statements
 */
@Component
public class StatementGenerationTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationTasklet.class);

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public StatementGenerationTasklet(AccountRepository accountRepository,
                                      CardXrefRepository cardXrefRepository,
                                      TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<Account> accounts = accountRepository.findAll();
        Path stmtDir = Paths.get("statements");

        try {
            Files.createDirectories(stmtDir);

            for (Account account : accounts) {
                List<CardXref> xrefs = cardXrefRepository.findByAcctId(account.getAcctId());
                List<Transaction> transactions = xrefs.stream()
                        .flatMap(xref -> transactionRepository.findByCardNum(xref.getCardNum()).stream())
                        .toList();

                generateTextStatement(stmtDir, account, transactions);
                generateHtmlStatement(stmtDir, account, transactions);
            }

            log.info("Statement generation complete: {} accounts processed", accounts.size());
        } catch (IOException e) {
            log.error("Failed to generate statements", e);
            throw new RuntimeException("Failed to generate statements", e);
        }

        return RepeatStatus.FINISHED;
    }

    private void generateTextStatement(Path dir, Account account, List<Transaction> transactions) throws IOException {
        String filename = String.format("stmt_%d_%s.txt", account.getAcctId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        try (PrintWriter writer = new PrintWriter(new FileWriter(dir.resolve(filename).toFile()))) {
            writer.println("CARDEMO ACCOUNT STATEMENT");
            writer.printf("Account: %d   Status: %s%n", account.getAcctId(), account.getActiveStatus());
            writer.printf("Balance: $%,.2f   Credit Limit: $%,.2f%n",
                    account.getCurrentBalance(), account.getCreditLimit());
            writer.println("-".repeat(70));

            for (Transaction txn : transactions) {
                writer.printf("%-16s %12.2f  %s%n",
                        txn.getOrigTimestamp() != null ? txn.getOrigTimestamp().substring(0, 10) : "",
                        txn.getAmount(), txn.getDescription());
            }
        }
    }

    private void generateHtmlStatement(Path dir, Account account, List<Transaction> transactions) throws IOException {
        String filename = String.format("stmt_%d_%s.html", account.getAcctId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        try (PrintWriter writer = new PrintWriter(new FileWriter(dir.resolve(filename).toFile()))) {
            writer.println("<html><head><title>Account Statement</title></head><body>");
            writer.printf("<h1>Account Statement - %d</h1>%n", account.getAcctId());
            writer.printf("<p>Status: %s | Balance: $%,.2f | Credit Limit: $%,.2f</p>%n",
                    account.getActiveStatus(), account.getCurrentBalance(), account.getCreditLimit());
            writer.println("<table border='1'><tr><th>Date</th><th>Amount</th><th>Description</th></tr>");

            for (Transaction txn : transactions) {
                writer.printf("<tr><td>%s</td><td>%.2f</td><td>%s</td></tr>%n",
                        txn.getOrigTimestamp() != null ? txn.getOrigTimestamp().substring(0, 10) : "",
                        txn.getAmount(), txn.getDescription());
            }

            writer.println("</table></body></html>");
        }
    }
}
