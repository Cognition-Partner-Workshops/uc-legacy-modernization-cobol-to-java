package com.carddemo.batch;

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

@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    // --- Transaction Validation Job (CBTRN01C.cbl + COMBTRAN.jcl) ---
    @Bean
    public Job transactionValidationJob(Step transactionValidationStep) {
        return new JobBuilder("transactionValidationJob", jobRepository)
                .start(transactionValidationStep)
                .build();
    }

    @Bean
    public Step transactionValidationStep(TransactionValidationTasklet tasklet) {
        return new StepBuilder("transactionValidationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // --- Transaction Posting Job (CBTRN02C.cbl + POSTTRAN.jcl) ---
    @Bean
    public Job transactionPostingJob(Step transactionPostingStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(transactionPostingStep)
                .build();
    }

    @Bean
    public Step transactionPostingStep(TransactionPostingTasklet tasklet) {
        return new StepBuilder("transactionPostingStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // --- Interest Calculation Job (CBACT04C.cbl + INTCALC.jcl) ---
    @Bean
    public Job interestCalculationJob(Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep)
                .build();
    }

    @Bean
    public Step interestCalculationStep(InterestCalculationTasklet tasklet) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // --- Transaction Report Job (CBTRN03C.cbl + TRANREPT.jcl) ---
    @Bean
    public Job transactionReportJob(Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep)
                .build();
    }

    @Bean
    public Step transactionReportStep(TransactionReportTasklet tasklet) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // --- Statement Generation Job (CBSTM03A.CBL + CBSTM03B.CBL + CREASTMT.JCL) ---
    @Bean
    public Job statementGenerationJob(Step statementGenerationStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(statementGenerationStep)
                .build();
    }

    @Bean
    public Step statementGenerationStep(StatementGenerationTasklet tasklet) {
        return new StepBuilder("statementGenerationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // --- Customer Report Job (CBCUS01C.cbl) ---
    @Bean
    public Job customerReportJob(Step customerReportStep) {
        return new JobBuilder("customerReportJob", jobRepository)
                .start(customerReportStep)
                .build();
    }

    @Bean
    public Step customerReportStep(CustomerReportTasklet tasklet) {
        return new StepBuilder("customerReportStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // --- Data Export Job (CBEXPORT.cbl + CBEXPORT.jcl) ---
    @Bean
    public Job dataExportJob(Step dataExportStep) {
        return new JobBuilder("dataExportJob", jobRepository)
                .start(dataExportStep)
                .build();
    }

    @Bean
    public Step dataExportStep(DataExportTasklet tasklet) {
        return new StepBuilder("dataExportStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // --- Data Import Job (CBIMPORT.cbl + CBIMPORT.jcl) ---
    @Bean
    public Job dataImportJob(Step dataImportStep) {
        return new JobBuilder("dataImportJob", jobRepository)
                .start(dataImportStep)
                .build();
    }

    @Bean
    public Step dataImportStep(DataImportTasklet tasklet) {
        return new StepBuilder("dataImportStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // --- File Initialization Job (replaces IDCAMS JCL jobs) ---
    @Bean
    public Job fileInitializationJob(Step fileInitializationStep) {
        return new JobBuilder("fileInitializationJob", jobRepository)
                .start(fileInitializationStep)
                .build();
    }

    @Bean
    public Step fileInitializationStep() {
        Tasklet tasklet = (contribution, chunkContext) -> {
            // File initialization is handled by Flyway migrations
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("fileInitializationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
