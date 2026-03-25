package com.cardemo.batch;

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

/**
 * Spring Batch Job Configuration - replaces JCL job definitions
 * 
 * Original JCL jobs converted:
 * - ACTRPT: Account/Card/Xref file reads (CBACT01C, CBACT02C, CBACT03C)
 * - INTCALC: Interest calculation (CBACT04C)
 * - CUSTRPT: Customer file read (CBCUS01C)
 * - TRNREAD: Transaction file read (CBTRN01C)
 * - TRNPOST: Transaction posting (CBTRN02C)
 * - TRNRPT: Transaction report (CBTRN03C - handled by ReportService)
 * - EXPORT: Data export (CBEXPORT)
 */
@Configuration
public class BatchJobConfig {

    /**
     * Account Report Job - replaces ACTRPT JCL job.
     * Steps: Read accounts -> Read cards -> Read cross-references
     */
    @Bean
    public Job accountReportJob(JobRepository jobRepository,
                                Step readAccountsStep,
                                Step readCardsStep,
                                Step readXrefsStep) {
        return new JobBuilder("accountReportJob", jobRepository)
                .start(readAccountsStep)
                .next(readCardsStep)
                .next(readXrefsStep)
                .build();
    }

    @Bean
    public Step readAccountsStep(JobRepository jobRepository,
                                 PlatformTransactionManager txManager,
                                 AccountDataProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            processor.processAllAccounts();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("readAccountsStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    @Bean
    public Step readCardsStep(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              AccountDataProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            processor.processAllCards();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("readCardsStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    @Bean
    public Step readXrefsStep(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              AccountDataProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            processor.processAllCardXrefs();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("readXrefsStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    /**
     * Interest Calculation Job - replaces INTCALC JCL job.
     */
    @Bean
    public Job interestCalculationJob(JobRepository jobRepository,
                                      Step calculateInterestStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(calculateInterestStep)
                .build();
    }

    @Bean
    public Step calculateInterestStep(JobRepository jobRepository,
                                      PlatformTransactionManager txManager,
                                      InterestCalculator calculator) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            calculator.calculateInterest();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("calculateInterestStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    /**
     * Customer Report Job - replaces CUSTRPT JCL job.
     */
    @Bean
    public Job customerReportJob(JobRepository jobRepository,
                                 Step readCustomersStep) {
        return new JobBuilder("customerReportJob", jobRepository)
                .start(readCustomersStep)
                .build();
    }

    @Bean
    public Step readCustomersStep(JobRepository jobRepository,
                                  PlatformTransactionManager txManager,
                                  CustomerDataProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            processor.processAllCustomers();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("readCustomersStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    /**
     * Transaction Posting Job - replaces TRNPOST JCL job.
     * Steps: Read transactions -> Post daily transactions
     */
    @Bean
    public Job transactionPostingJob(JobRepository jobRepository,
                                     Step readTransactionsStep,
                                     Step postDailyTransactionsStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(readTransactionsStep)
                .next(postDailyTransactionsStep)
                .build();
    }

    @Bean
    public Step readTransactionsStep(JobRepository jobRepository,
                                     PlatformTransactionManager txManager,
                                     TransactionProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            processor.readAllTransactions();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("readTransactionsStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    @Bean
    public Step postDailyTransactionsStep(JobRepository jobRepository,
                                          PlatformTransactionManager txManager,
                                          TransactionProcessor processor) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            processor.postDailyTransactions();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("postDailyTransactionsStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    /**
     * Data Export Job - replaces EXPORT JCL job.
     */
    @Bean
    public Job dataExportJob(JobRepository jobRepository,
                             Step exportDataStep) {
        return new JobBuilder("dataExportJob", jobRepository)
                .start(exportDataStep)
                .build();
    }

    @Bean
    public Step exportDataStep(JobRepository jobRepository,
                               PlatformTransactionManager txManager,
                               DataExporter exporter) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            exporter.exportAll();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("exportDataStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }
}
