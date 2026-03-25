package com.carddemo.batch.job;

import com.carddemo.batch.repository.CardCrossReferenceRepository;
import com.carddemo.batch.repository.TransactionCategoryRepository;
import com.carddemo.batch.repository.TransactionRepository;
import com.carddemo.batch.repository.TransactionTypeRepository;
import com.carddemo.common.model.CardCrossReference;
import com.carddemo.common.model.Transaction;
import com.carddemo.common.model.TransactionCategory;
import com.carddemo.common.model.TransactionType;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Batch job replacing COBOL program CBTRN03C / JCL job TRANREPT.
 * Generates a daily transaction detail report.
 *
 * Original COBOL: app/cbl/CBTRN03C.cbl
 * JCL Job: TRANREPT
 *
 * Processing logic (from COBOL):
 * 1. Accept date range parameters (start/end date)
 * 2. Read transactions in order
 * 3. Filter transactions within date range
 * 4. For each transaction, look up type and category descriptions
 * 5. Look up account ID via card cross-reference
 * 6. Format and print report with:
 *    - Report header with date range
 *    - Column headers
 *    - Transaction detail lines
 *    - Page totals, account totals, grand total
 */
@Configuration
public class TransactionReportJob {

    private static final Logger log = LoggerFactory.getLogger(TransactionReportJob.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int PAGE_SIZE = 50;

    private final TransactionRepository transactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final CardCrossReferenceRepository cardCrossReferenceRepository;

    public TransactionReportJob(
            TransactionRepository transactionRepository,
            TransactionTypeRepository transactionTypeRepository,
            TransactionCategoryRepository transactionCategoryRepository,
            CardCrossReferenceRepository cardCrossReferenceRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
        this.cardCrossReferenceRepository = cardCrossReferenceRepository;
    }

    @Bean
    public Job transactionReportBatchJob(JobRepository jobRepository,
                                         Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep)
                .build();
    }

    @Bean
    public Step transactionReportStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .tasklet(new TransactionReportTasklet(), transactionManager)
                .build();
    }

    private class TransactionReportTasklet implements Tasklet {

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            log.info("START OF EXECUTION OF PROGRAM CBTRN03C (TransactionReportJob)");

            String startDateStr = (String) chunkContext.getStepContext()
                    .getJobParameters().get("startDate");
            String endDateStr = (String) chunkContext.getStepContext()
                    .getJobParameters().get("endDate");

            LocalDate startDate = startDateStr != null
                    ? LocalDate.parse(startDateStr, DATE_FORMAT)
                    : LocalDate.now().minusDays(1);
            LocalDate endDate = endDateStr != null
                    ? LocalDate.parse(endDateStr, DATE_FORMAT)
                    : LocalDate.now();

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            log.info("Report date range: {} to {}", startDate, endDate);

            List<Transaction> transactions = transactionRepository
                    .findByProcessedTimestampBetweenOrderByCardNumberAscTransactionIdAsc(
                            startDateTime, endDateTime);

            StringBuilder report = new StringBuilder();

            // Report header (mirrors CVTRA07Y.cpy REPORT-NAME-HEADER)
            report.append(String.format("%-38s%-41s%-12s%-10s to %-10s%n",
                    "DALYREPT", "Daily Transaction Report", "Date Range: ",
                    startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT)));
            report.append("=".repeat(133)).append("\n");

            // Column headers (mirrors CVTRA07Y.cpy TRANSACTION-HEADER-1)
            report.append(String.format("%-17s%-12s%-19s%-35s%-14s %16s%n",
                    "Transaction ID", "Account ID", "Transaction Type",
                    "Tran Category", "Tran Source", "Amount"));
            report.append("-".repeat(133)).append("\n");

            BigDecimal grandTotal = BigDecimal.ZERO;
            BigDecimal pageTotal = BigDecimal.ZERO;
            BigDecimal accountTotal = BigDecimal.ZERO;
            String currentAccountId = "";
            int lineCount = 0;

            for (Transaction tran : transactions) {
                // Look up account ID via card cross-reference
                String accountId = "";
                if (tran.getCardNumber() != null) {
                    Optional<CardCrossReference> xrefOpt =
                            cardCrossReferenceRepository.findById(tran.getCardNumber());
                    if (xrefOpt.isPresent()) {
                        accountId = String.valueOf(xrefOpt.get().getAccountId());
                    }
                }

                // Account break
                if (!accountId.equals(currentAccountId) && !currentAccountId.isEmpty()) {
                    report.append(String.format("%-13s%84s%+15.2f%n",
                            "Account Total", ".".repeat(84), accountTotal));
                    accountTotal = BigDecimal.ZERO;
                }
                currentAccountId = accountId;

                // Page break
                if (lineCount > 0 && lineCount % PAGE_SIZE == 0) {
                    report.append(String.format("%-11s%86s%+15.2f%n",
                            "Page Total", ".".repeat(86), pageTotal));
                    report.append("-".repeat(133)).append("\n");
                    pageTotal = BigDecimal.ZERO;
                }

                // Look up type description
                String typeDesc = "";
                if (tran.getTypeCode() != null) {
                    Optional<TransactionType> typeOpt =
                            transactionTypeRepository.findById(tran.getTypeCode());
                    if (typeOpt.isPresent()) {
                        typeDesc = typeOpt.get().getTypeDescription() != null
                                ? typeOpt.get().getTypeDescription().trim() : "";
                        if (typeDesc.length() > 15) typeDesc = typeDesc.substring(0, 15);
                    }
                }

                // Look up category description
                String catDesc = "";
                if (tran.getTypeCode() != null && tran.getCategoryCode() != null) {
                    Optional<TransactionCategory> catOpt =
                            transactionCategoryRepository.findByTypeCodeAndCategoryCode(
                                    tran.getTypeCode(), tran.getCategoryCode());
                    if (catOpt.isPresent()) {
                        catDesc = catOpt.get().getCategoryDescription() != null
                                ? catOpt.get().getCategoryDescription().trim() : "";
                        if (catDesc.length() > 29) catDesc = catDesc.substring(0, 29);
                    }
                }

                BigDecimal amount = tran.getAmount() != null ? tran.getAmount() : BigDecimal.ZERO;
                grandTotal = grandTotal.add(amount);
                pageTotal = pageTotal.add(amount);
                accountTotal = accountTotal.add(amount);

                // Transaction detail line (mirrors CVTRA07Y.cpy TRANSACTION-DETAIL-REPORT)
                report.append(String.format("%-16s %-11s %-2s-%-15s %-4s-%-29s %-10s    %+15.2f%n",
                        tran.getTransactionId() != null ? tran.getTransactionId() : "",
                        accountId,
                        tran.getTypeCode() != null ? tran.getTypeCode() : "",
                        typeDesc,
                        tran.getCategoryCode() != null ? tran.getCategoryCode() : "",
                        catDesc,
                        tran.getSource() != null ? tran.getSource() : "",
                        amount));

                lineCount++;
            }

            // Final account total
            if (!currentAccountId.isEmpty()) {
                report.append(String.format("%-13s%84s%+15.2f%n",
                        "Account Total", ".".repeat(84), accountTotal));
            }

            // Final page total
            report.append(String.format("%-11s%86s%+15.2f%n",
                    "Page Total", ".".repeat(86), pageTotal));

            // Grand total (mirrors CVTRA07Y.cpy REPORT-GRAND-TOTALS)
            report.append("=".repeat(133)).append("\n");
            report.append(String.format("%-11s%86s%+15.2f%n",
                    "Grand Total", ".".repeat(86), grandTotal));

            log.info("TRANSACTION REPORT:\n{}", report);
            log.info("TRANSACTIONS IN REPORT: {}", lineCount);
            log.info("END OF EXECUTION OF PROGRAM CBTRN03C (TransactionReportJob)");
            return RepeatStatus.FINISHED;
        }
    }
}
