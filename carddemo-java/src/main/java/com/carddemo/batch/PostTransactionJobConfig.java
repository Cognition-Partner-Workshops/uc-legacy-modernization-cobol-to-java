package com.carddemo.batch;

import com.carddemo.model.DailyTransaction;
import com.carddemo.model.Transaction;
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

/**
 * Spring Batch job replacing JCL POSTTRAN.jcl and COBOL programs CBTRN01C + CBTRN02C.
 *
 * <p>Original JCL flow (POSTTRAN.jcl):
 * <pre>
 * //POSTTRAN JOB ...
 * //STEP01   EXEC PGM=CBTRN01C    (Validate daily transactions)
 * //STEP02   EXEC PGM=CBTRN02C    (Post validated transactions)
 * //  DD DALYTRAN  -> daily_transactions (input)
 * //  DD XREFFILE  -> card_xref (lookup)
 * //  DD ACCTDAT   -> accounts (update balances)
 * //  DD TRANSACT  -> transactions (output)
 * //  DD DALYRJCT  -> rejected transactions (output)
 * </pre>
 *
 * <p>CBTRN02C (732 lines) logic:
 * 1. Read daily transaction record
 * 2. Lookup card in XREFFILE to find account
 * 3. Validate: card exists, account active, not expired, within credit limit
 * 4. If valid: write to TRANSACT, update account balance in ACCTDAT
 * 5. If invalid: write to DALYRJCT reject file
 *
 * <p>Phase 2 will implement the full ItemReader/Processor/Writer logic.
 */
@Configuration
public class PostTransactionJobConfig {

    @Bean
    public Job postTransactionJob(JobRepository jobRepository, Step validateAndPostStep) {
        return new JobBuilder("postTransactionJob", jobRepository)
                .start(validateAndPostStep)
                .build();
    }

    @Bean
    public Step validateAndPostStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     ItemReader<DailyTransaction> dailyTransactionReader,
                                     ItemProcessor<DailyTransaction, Transaction> transactionProcessor,
                                     ItemWriter<Transaction> transactionWriter) {
        return new StepBuilder("validateAndPostStep", jobRepository)
                .<DailyTransaction, Transaction>chunk(10, transactionManager)
                .reader(dailyTransactionReader)
                .processor(transactionProcessor)
                .writer(transactionWriter)
                .build();
    }

    /**
     * Reads unprocessed daily transactions.
     * Replaces CBTRN02C OPEN/READ on DALYTRAN DD.
     */
    @Bean
    public ItemReader<DailyTransaction> dailyTransactionReader() {
        // Phase 2: implement JPA-based reader for unprocessed daily transactions
        // SELECT * FROM daily_transactions WHERE processed = false
        return () -> null; // No-op stub - returns null to signal end of input
    }

    /**
     * Validates and transforms daily transactions into posted transactions.
     * Replaces CBTRN02C validation paragraphs (XREF lookup, account check, credit limit).
     */
    @Bean
    public ItemProcessor<DailyTransaction, Transaction> transactionProcessor() {
        // Phase 2: implement full validation logic from CBTRN02C
        // 1. Lookup card in card_xref
        // 2. Validate account is active
        // 3. Check card not expired
        // 4. Verify within credit limit
        // 5. Transform DailyTransaction -> Transaction
        return item -> null; // No-op stub
    }

    /**
     * Writes posted transactions and updates account balances.
     * Replaces CBTRN02C WRITE to TRANSACT and REWRITE to ACCTDAT.
     */
    @Bean
    public ItemWriter<Transaction> transactionWriter() {
        // Phase 2: implement writer that:
        // 1. Saves Transaction to transactions table
        // 2. Updates account balance in accounts table
        // 3. Marks DailyTransaction as processed
        return items -> { }; // No-op stub
    }
}
