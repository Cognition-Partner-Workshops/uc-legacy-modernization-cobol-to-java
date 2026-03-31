package com.carddemo.batch.job;

import com.carddemo.batch.model.RejectedTransaction;
import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardCrossReferenceRepository;
import com.carddemo.batch.repository.DailyTransactionRepository;
import com.carddemo.batch.repository.TransactionCategoryBalanceRepository;
import com.carddemo.batch.repository.TransactionRepository;
import com.carddemo.common.model.Account;
import com.carddemo.common.model.CardCrossReference;
import com.carddemo.common.model.DailyTransaction;
import com.carddemo.common.model.Transaction;
import com.carddemo.common.model.TransactionCategoryBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Batch job replacing COBOL program CBTRN02C / JCL job POSTTRAN.
 * Processes daily transactions and posts them to the transaction file.
 *
 * Original COBOL: app/cbl/CBTRN02C.cbl
 * JCL Job: POSTTRAN
 */
@Configuration
public class TransactionPostingJob {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingJob.class);

    private final DailyTransactionRepository dailyTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CardCrossReferenceRepository cardCrossReferenceRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository transactionCategoryBalanceRepository;

    public TransactionPostingJob(
            DailyTransactionRepository dailyTransactionRepository,
            TransactionRepository transactionRepository,
            CardCrossReferenceRepository cardCrossReferenceRepository,
            AccountRepository accountRepository,
            TransactionCategoryBalanceRepository transactionCategoryBalanceRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.cardCrossReferenceRepository = cardCrossReferenceRepository;
        this.accountRepository = accountRepository;
        this.transactionCategoryBalanceRepository = transactionCategoryBalanceRepository;
    }

    @Bean
    public Job transactionPostingBatchJob(JobRepository jobRepository,
                                          Step transactionPostingStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(transactionPostingStep)
                .build();
    }

    @Bean
    public Step transactionPostingStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
        return new StepBuilder("transactionPostingStep", jobRepository)
                .tasklet(new TransactionPostingTasklet(), transactionManager)
                .build();
    }

    private class TransactionPostingTasklet implements Tasklet {

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            log.info("START OF EXECUTION OF PROGRAM CBTRN02C (TransactionPostingJob)");

            List<DailyTransaction> dailyTransactions =
                    dailyTransactionRepository.findAllByOrderByTransactionIdAsc();

            int transactionCount = 0;
            int rejectCount = 0;
            List<RejectedTransaction> rejectedTransactions = new ArrayList<>();

            for (DailyTransaction dailyTran : dailyTransactions) {
                transactionCount++;
                ValidationResult validation = validateTransaction(dailyTran);

                if (validation.isValid()) {
                    postTransaction(dailyTran, validation.crossReference(), validation.account());
                } else {
                    rejectCount++;
                    rejectedTransactions.add(new RejectedTransaction(
                            dailyTran.getTransactionId(),
                            dailyTran.getCardNumber(),
                            validation.failureReasonCode(),
                            validation.failureReasonDescription()));
                    log.warn("Transaction {} rejected: {} - {}",
                            dailyTran.getTransactionId(),
                            validation.failureReasonCode(),
                            validation.failureReasonDescription());
                }
            }

            log.info("TRANSACTIONS PROCESSED: {}", transactionCount);
            log.info("TRANSACTIONS REJECTED:  {}", rejectCount);

            if (rejectCount > 0) {
                contribution.setExitStatus(new ExitStatus("COMPLETED_WITH_REJECTS",
                        "Rejected " + rejectCount + " of " + transactionCount));
            }

            log.info("END OF EXECUTION OF PROGRAM CBTRN02C (TransactionPostingJob)");
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Validates a daily transaction per COBOL paragraph 1500-VALIDATE-TRAN.
     */
    private ValidationResult validateTransaction(DailyTransaction dailyTran) {
        // 1500-A-LOOKUP-XREF
        Optional<CardCrossReference> xrefOpt =
                cardCrossReferenceRepository.findById(dailyTran.getCardNumber());
        if (xrefOpt.isEmpty()) {
            return ValidationResult.rejected(100, "INVALID CARD NUMBER FOUND");
        }
        CardCrossReference xref = xrefOpt.get();

        // 1500-B-LOOKUP-ACCT
        Optional<Account> accountOpt = accountRepository.findById(xref.getAccountId());
        if (accountOpt.isEmpty()) {
            return ValidationResult.rejected(101, "ACCOUNT RECORD NOT FOUND");
        }
        Account account = accountOpt.get();

        // Overlimit check
        BigDecimal cycleCredit = account.getCurrentCycleCredit() != null
                ? account.getCurrentCycleCredit() : BigDecimal.ZERO;
        BigDecimal cycleDebit = account.getCurrentCycleDebit() != null
                ? account.getCurrentCycleDebit() : BigDecimal.ZERO;
        BigDecimal tranAmt = dailyTran.getAmount() != null
                ? dailyTran.getAmount() : BigDecimal.ZERO;
        BigDecimal tempBal = cycleCredit.subtract(cycleDebit).add(tranAmt);
        BigDecimal creditLimit = account.getCreditLimit() != null
                ? account.getCreditLimit() : BigDecimal.ZERO;
        if (creditLimit.compareTo(tempBal) < 0) {
            return ValidationResult.rejected(102, "OVERLIMIT TRANSACTION");
        }

        // Expiration check
        if (account.getExpirationDate() != null && dailyTran.getOriginTimestamp() != null) {
            LocalDate tranDate = dailyTran.getOriginTimestamp().toLocalDate();
            if (account.getExpirationDate().isBefore(tranDate)) {
                return ValidationResult.rejected(103,
                        "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
            }
        }

        return ValidationResult.valid(xref, account);
    }

    /**
     * Posts a valid transaction per COBOL paragraph 2000-POST-TRANSACTION.
     */
    private void postTransaction(DailyTransaction dailyTran,
                                 CardCrossReference xref, Account account) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(dailyTran.getTransactionId());
        transaction.setTypeCode(dailyTran.getTypeCode());
        transaction.setCategoryCode(dailyTran.getCategoryCode());
        transaction.setSource(dailyTran.getSource());
        transaction.setDescription(dailyTran.getDescription());
        transaction.setAmount(dailyTran.getAmount());
        transaction.setMerchantId(dailyTran.getMerchantId());
        transaction.setMerchantName(dailyTran.getMerchantName());
        transaction.setMerchantCity(dailyTran.getMerchantCity());
        transaction.setMerchantZip(dailyTran.getMerchantZip());
        transaction.setCardNumber(dailyTran.getCardNumber());
        transaction.setOriginTimestamp(dailyTran.getOriginTimestamp());
        transaction.setProcessedTimestamp(LocalDateTime.now());

        // 2700-UPDATE-TCATBAL
        updateTransactionCategoryBalance(xref.getAccountId(),
                dailyTran.getTypeCode(), dailyTran.getCategoryCode(), dailyTran.getAmount());

        // 2800-UPDATE-ACCOUNT-REC
        BigDecimal tranAmt = dailyTran.getAmount() != null
                ? dailyTran.getAmount() : BigDecimal.ZERO;
        BigDecimal currentBal = account.getCurrentBalance() != null
                ? account.getCurrentBalance() : BigDecimal.ZERO;
        account.setCurrentBalance(currentBal.add(tranAmt));

        if (tranAmt.compareTo(BigDecimal.ZERO) >= 0) {
            BigDecimal cycCredit = account.getCurrentCycleCredit() != null
                    ? account.getCurrentCycleCredit() : BigDecimal.ZERO;
            account.setCurrentCycleCredit(cycCredit.add(tranAmt));
        } else {
            BigDecimal cycDebit = account.getCurrentCycleDebit() != null
                    ? account.getCurrentCycleDebit() : BigDecimal.ZERO;
            account.setCurrentCycleDebit(cycDebit.add(tranAmt.abs()));
        }

        // 2900-WRITE-TRANSACTION-FILE
        transactionRepository.save(transaction);
        accountRepository.save(account);
    }

    /**
     * Updates or creates a transaction category balance per COBOL paragraph 2700-UPDATE-TCATBAL.
     */
    private void updateTransactionCategoryBalance(Long accountId, String typeCode,
                                                   String categoryCode, BigDecimal amount) {
        Optional<TransactionCategoryBalance> existingOpt =
                transactionCategoryBalanceRepository.findByAccountIdAndTypeCodeAndCategoryCode(
                        accountId, typeCode, categoryCode);

        if (existingOpt.isPresent()) {
            TransactionCategoryBalance existing = existingOpt.get();
            BigDecimal currentBal = existing.getBalance() != null
                    ? existing.getBalance() : BigDecimal.ZERO;
            existing.setBalance(currentBal.add(amount != null ? amount : BigDecimal.ZERO));
            transactionCategoryBalanceRepository.save(existing);
        } else {
            TransactionCategoryBalance newBalance = new TransactionCategoryBalance();
            newBalance.setAccountId(accountId);
            newBalance.setTypeCode(typeCode);
            newBalance.setCategoryCode(categoryCode);
            newBalance.setBalance(amount != null ? amount : BigDecimal.ZERO);
            transactionCategoryBalanceRepository.save(newBalance);
        }
    }

    private record ValidationResult(
            boolean isValid,
            int failureReasonCode,
            String failureReasonDescription,
            CardCrossReference crossReference,
            Account account
    ) {
        static ValidationResult valid(CardCrossReference xref, Account account) {
            return new ValidationResult(true, 0, "", xref, account);
        }

        static ValidationResult rejected(int code, String description) {
            return new ValidationResult(false, code, description, null, null);
        }
    }
}
