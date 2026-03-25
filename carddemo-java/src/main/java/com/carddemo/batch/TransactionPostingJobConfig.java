package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.TranCatBalance;
import com.carddemo.entity.TranCatBalanceId;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TranCatBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * Transaction posting batch job - replaces CBTRN02C (732 lines).
 * Original COBOL: reads DALYTRAN sequential file, validates each transaction against
 * XREF, ACCOUNT files, posts valid transactions to TRANSACT, writes rejects to DALYREJS,
 * updates TCATBAL category balances.
 *
 * Spring Batch: reads from daily_transactions table (status=PENDING), processes with
 * validation, writes to transactions table, updates account balances and category balances.
 */
@Configuration
public class TransactionPostingJobConfig {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingJobConfig.class);

    private final DailyTransactionRepository dailyTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TranCatBalanceRepository tranCatBalanceRepository;

    public TransactionPostingJobConfig(DailyTransactionRepository dailyTransactionRepository,
                                        TransactionRepository transactionRepository,
                                        CardXrefRepository cardXrefRepository,
                                        AccountRepository accountRepository,
                                        TranCatBalanceRepository tranCatBalanceRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.tranCatBalanceRepository = tranCatBalanceRepository;
    }

    @Bean
    public Job transactionPostingJob(JobRepository jobRepository, Step postTransactionsStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(postTransactionsStep)
                .build();
    }

    @Bean
    public Step postTransactionsStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("postTransactionsStep", jobRepository)
                .<DailyTransaction, Transaction>chunk(100, transactionManager)
                .reader(dailyTransactionReader())
                .processor(transactionProcessor())
                .writer(transactionWriter())
                .faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(TransactionRejectedException.class)
                .build();
    }

    @Bean
    public ItemReader<DailyTransaction> dailyTransactionReader() {
        return new ItemReader<>() {
            private Iterator<DailyTransaction> iterator;

            @Override
            public DailyTransaction read() {
                if (iterator == null) {
                    List<DailyTransaction> pending = dailyTransactionRepository.findByStatus("PENDING");
                    iterator = pending.iterator();
                }
                return iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    @Bean
    public ItemProcessor<DailyTransaction, Transaction> transactionProcessor() {
        return dailyTran -> {
            // Validate card cross-reference exists
            CardXref xref = cardXrefRepository.findByCardNumber(dailyTran.getCardNumber())
                    .orElse(null);
            if (xref == null) {
                rejectTransaction(dailyTran, "Card cross-reference not found");
                throw new TransactionRejectedException("No xref for card: " + dailyTran.getCardNumber());
            }

            // Validate account exists and is active
            Account account = accountRepository.findById(xref.getAccountId()).orElse(null);
            if (account == null) {
                rejectTransaction(dailyTran, "Account not found");
                throw new TransactionRejectedException("Account not found: " + xref.getAccountId());
            }
            if (!"Y".equals(account.getActiveStatus())) {
                rejectTransaction(dailyTran, "Account not active");
                throw new TransactionRejectedException("Account not active: " + xref.getAccountId());
            }

            // Convert to posted transaction
            Transaction posted = new Transaction();
            posted.setTransactionId(dailyTran.getTransactionId());
            posted.setTypeCode(dailyTran.getTypeCode());
            posted.setCategoryCode(dailyTran.getCategoryCode());
            posted.setSource(dailyTran.getSource());
            posted.setDescription(dailyTran.getDescription());
            posted.setAmount(dailyTran.getAmount());
            posted.setMerchantId(dailyTran.getMerchantId());
            posted.setMerchantName(dailyTran.getMerchantName());
            posted.setMerchantCity(dailyTran.getMerchantCity());
            posted.setMerchantZip(dailyTran.getMerchantZip());
            posted.setCardNumber(dailyTran.getCardNumber());
            posted.setOriginatedTs(dailyTran.getOriginatedTs());
            posted.setProcessedTs(LocalDateTime.now());

            return posted;
        };
    }

    @Bean
    public ItemWriter<Transaction> transactionWriter() {
        return transactions -> {
            for (Transaction tran : transactions) {
                transactionRepository.save(tran);

                // Update account balance
                CardXref xref = cardXrefRepository.findByCardNumber(tran.getCardNumber()).orElse(null);
                if (xref != null) {
                    Account account = accountRepository.findById(xref.getAccountId()).orElse(null);
                    if (account != null) {
                        account.setCurrentBalance(account.getCurrentBalance().add(tran.getAmount()));
                        accountRepository.save(account);
                    }
                }

                // Update category balance
                updateCategoryBalance(tran);

                // Mark daily transaction as posted
                dailyTransactionRepository.findById(tran.getTransactionId()).ifPresent(dt -> {
                    dt.setStatus("POSTED");
                    dt.setProcessedTs(LocalDateTime.now());
                    dailyTransactionRepository.save(dt);
                });

                log.info("Posted transaction: {}", tran.getTransactionId());
            }
        };
    }

    private void updateCategoryBalance(Transaction tran) {
        CardXref xref = cardXrefRepository.findByCardNumber(tran.getCardNumber()).orElse(null);
        if (xref == null) return;

        TranCatBalanceId balId = new TranCatBalanceId(
                xref.getAccountId(), tran.getTypeCode(), tran.getCategoryCode());

        TranCatBalance balance = tranCatBalanceRepository.findById(balId)
                .orElse(new TranCatBalance(xref.getAccountId(), tran.getTypeCode(),
                        tran.getCategoryCode(), BigDecimal.ZERO));

        balance.setBalance(balance.getBalance().add(tran.getAmount()));
        tranCatBalanceRepository.save(balance);
    }

    private void rejectTransaction(DailyTransaction dailyTran, String reason) {
        dailyTran.setStatus("REJECTED");
        dailyTran.setRejectionReason(reason);
        dailyTran.setProcessedTs(LocalDateTime.now());
        dailyTransactionRepository.save(dailyTran);
        log.warn("Rejected transaction {}: {}", dailyTran.getTransactionId(), reason);
    }

    public static class TransactionRejectedException extends RuntimeException {
        public TransactionRejectedException(String message) {
            super(message);
        }
    }
}
