package com.carddemo.batch;

import com.carddemo.entity.Transaction;
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
 * Transaction Report - from CBTRN03C.cbl + TRANREPT.jcl
 * Read transactions, format report with page/account/grand totals
 */
@Component
public class TransactionReportTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(TransactionReportTasklet.class);

    private final TransactionRepository transactionRepository;

    public TransactionReportTasklet(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String startDate = chunkContext.getStepContext().getJobParameters()
                .getOrDefault("startDate", "").toString();
        String endDate = chunkContext.getStepContext().getJobParameters()
                .getOrDefault("endDate", "").toString();

        List<Transaction> transactions = transactionRepository.findAll();

        // Filter by date range if provided
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> t.getOrigTimestamp() != null
                            && t.getOrigTimestamp().compareTo(startDate) >= 0
                            && t.getOrigTimestamp().compareTo(endDate) <= 0)
                    .toList();
        }

        Path reportDir = Paths.get("reports");
        try {
            Files.createDirectories(reportDir);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path reportFile = reportDir.resolve("transaction_report_" + timestamp + ".txt");

            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile.toFile()))) {
                writer.println("=".repeat(80));
                writer.println("CARDEMO TRANSACTION REPORT");
                writer.printf("Generated: %s%n", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writer.printf("Period: %s to %s%n", startDate.isEmpty() ? "ALL" : startDate, endDate.isEmpty() ? "ALL" : endDate);
                writer.println("=".repeat(80));
                writer.println();

                writer.printf("%-16s %-16s %-2s %-10s %12s %-30s%n",
                        "CARD NUM", "TRAN ID", "TP", "SOURCE", "AMOUNT", "DESCRIPTION");
                writer.println("-".repeat(80));

                BigDecimal grandTotal = BigDecimal.ZERO;
                for (Transaction txn : transactions) {
                    writer.printf("%-16s %-16s %-2s %-10s %12.2f %-30s%n",
                            txn.getCardNum(), txn.getTranId(), txn.getTypeCd(),
                            txn.getSource() != null ? txn.getSource() : "",
                            txn.getAmount(), txn.getDescription() != null ? txn.getDescription() : "");
                    grandTotal = grandTotal.add(txn.getAmount() != null ? txn.getAmount() : BigDecimal.ZERO);
                }

                writer.println("-".repeat(80));
                writer.printf("GRAND TOTAL: %,12.2f   TRANSACTION COUNT: %d%n", grandTotal, transactions.size());
                writer.println("=".repeat(80));
            }

            log.info("Transaction report generated: {} ({} transactions)", reportFile, transactions.size());
        } catch (IOException e) {
            log.error("Failed to generate transaction report", e);
            throw new RuntimeException("Failed to generate transaction report", e);
        }

        return RepeatStatus.FINISHED;
    }
}
