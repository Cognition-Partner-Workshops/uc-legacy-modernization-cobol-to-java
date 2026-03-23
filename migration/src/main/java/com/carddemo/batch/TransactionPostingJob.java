package com.carddemo.batch;

import com.carddemo.entity.Transaction;
import com.carddemo.service.TransactionPostingService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;

/**
 * Replaces POSTTRAN JCL / CBTRN02C.cbl daily batch job.
 * Reads daily transactions, validates, and posts valid ones.
 */
@Configuration
public class TransactionPostingJob {

    private final TransactionPostingService transactionPostingService;

    public TransactionPostingJob(TransactionPostingService transactionPostingService) {
        this.transactionPostingService = transactionPostingService;
    }

    @Bean
    public Job postTransactionsJob(JobRepository jobRepository, Step postTransactionsStep) {
        return new JobBuilder("postTransactionsJob", jobRepository)
                .start(postTransactionsStep)
                .build();
    }

    @Bean
    public Step postTransactionsStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("postTransactionsStep", jobRepository)
                .<Transaction, Transaction>chunk(10, transactionManager)
                .reader(dailyTransactionReader())
                .processor(transactionValidator())
                .writer(transactionWriter())
                .build();
    }

    @Bean
    public ItemReader<Transaction> dailyTransactionReader() {
        return new ListItemReader<>(new ArrayList<>());
    }

    @Bean
    public ItemProcessor<Transaction, Transaction> transactionValidator() {
        return transaction -> {
            TransactionPostingService.ValidationResult result =
                    transactionPostingService.validateTransaction(transaction);
            if (result.reasonCode() == 0) {
                return transaction;
            }
            return null; // filtered out
        };
    }

    @Bean
    public ItemWriter<Transaction> transactionWriter() {
        return transactions -> {
            for (Transaction tran : transactions) {
                transactionPostingService.postTransaction(tran);
            }
        };
    }
}
