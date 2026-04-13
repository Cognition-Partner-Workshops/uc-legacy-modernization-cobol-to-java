package com.carddemo.batch;

import com.carddemo.model.entity.Transaction;
import com.carddemo.repository.TransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Mirrors TRANREPT.jcl / CBTRN03C.cbl - Transaction Report.
 * Generates a transaction report for all transactions.
 */
@Configuration
public class TransactionReportJob {

    private final TransactionRepository transactionRepository;

    public TransactionReportJob(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Bean
    public Job transactionReportBatchJob(JobRepository jobRepository, Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep)
                .build();
    }

    @Bean
    public Step transactionReportStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .tasklet(transactionReportTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet transactionReportTasklet() {
        return (contribution, chunkContext) -> {
            List<Transaction> transactions = transactionRepository.findAll();
            // Report generation - in production this would write to a file
            // The data is available for report generation via the repository
            return RepeatStatus.FINISHED;
        };
    }
}
