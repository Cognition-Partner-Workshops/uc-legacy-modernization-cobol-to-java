package com.aws.carddemo.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchJobConfiguration {
  @Bean
  Job postTransactionsJob(
      JobRepository repository, @Qualifier("postTransactionsStep") Step postTransactionsStep) {
    return new JobBuilder("postTransactionsJob", repository).start(postTransactionsStep).build();
  }

  @Bean
  Job interestCalculationJob(
      JobRepository repository,
      @Qualifier("interestCalculationStep") Step interestCalculationStep) {
    return new JobBuilder("interestCalculationJob", repository)
        .start(interestCalculationStep)
        .build();
  }

  @Bean
  Job dailyTransactionValidateJob(
      JobRepository repository,
      @Qualifier("dailyTransactionValidateStep") Step dailyTransactionValidateStep) {
    return new JobBuilder("dailyTransactionValidateJob", repository)
        .start(dailyTransactionValidateStep)
        .build();
  }

  @Bean
  Job transactionReportJob(
      JobRepository repository, @Qualifier("transactionReportStep") Step transactionReportStep) {
    return new JobBuilder("transactionReportJob", repository).start(transactionReportStep).build();
  }

  @Bean
  Job createStatementsJob(
      JobRepository repository, @Qualifier("createStatementsStep") Step createStatementsStep) {
    return new JobBuilder("createStatementsJob", repository).start(createStatementsStep).build();
  }

  @Bean
  Job exportJob(JobRepository repository, @Qualifier("exportStep") Step exportStep) {
    return new JobBuilder("exportJob", repository).start(exportStep).build();
  }

  @Bean
  Job importJob(JobRepository repository, @Qualifier("importStep") Step importStep) {
    return new JobBuilder("importJob", repository).start(importStep).build();
  }

  @Bean
  Job combtranJob(JobRepository repository, @Qualifier("combtranStep") Step combtranStep) {
    return new JobBuilder("combtranJob", repository).start(combtranStep).build();
  }

  @Bean
  Job transactionBackupJob(
      JobRepository repository, @Qualifier("transactionBackupStep") Step transactionBackupStep) {
    return new JobBuilder("transactionBackupJob", repository).start(transactionBackupStep).build();
  }

  @Bean
  Job categoryBalancePrintJob(
      JobRepository repository,
      @Qualifier("categoryBalancePrintStep") Step categoryBalancePrintStep) {
    return new JobBuilder("categoryBalancePrintJob", repository)
        .start(categoryBalancePrintStep)
        .build();
  }

  @Bean
  Job authorizationPurgeJob(
      JobRepository repository, @Qualifier("authorizationPurgeStep") Step authorizationPurgeStep) {
    return new JobBuilder("authorizationPurgeJob", repository)
        .start(authorizationPurgeStep)
        .build();
  }

  @Bean
  Job transactionTypeUpdateJob(
      JobRepository repository,
      @Qualifier("transactionTypeUpdateStep") Step transactionTypeUpdateStep) {
    return new JobBuilder("transactionTypeUpdateJob", repository)
        .start(transactionTypeUpdateStep)
        .build();
  }

  @Bean
  Step postTransactionsStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      PostingTasklet tasklet) {
    return new StepBuilder("postTransactionsStep", repository)
        .tasklet(tasklet, transactionManager)
        .build();
  }

  @Bean
  Step interestCalculationStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      InterestTasklet tasklet) {
    return new StepBuilder("interestCalculationStep", repository)
        .tasklet(tasklet, transactionManager)
        .build();
  }

  @Bean
  Step dailyTransactionValidateStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      DailyValidationTasklet tasklet) {
    return new StepBuilder("dailyTransactionValidateStep", repository)
        .tasklet(tasklet, transactionManager)
        .build();
  }

  @Bean
  Step transactionReportStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      ReportTasklet tasklet) {
    return new StepBuilder("transactionReportStep", repository)
        .tasklet(tasklet, transactionManager)
        .build();
  }

  @Bean
  Step createStatementsStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      StatementTasklet tasklet) {
    return new StepBuilder("createStatementsStep", repository)
        .tasklet(tasklet, transactionManager)
        .build();
  }

  @Bean
  Step exportStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      ExportTasklet tasklet) {
    return new StepBuilder("exportStep", repository).tasklet(tasklet, transactionManager).build();
  }

  @Bean
  Step importStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      ImportTasklet tasklet) {
    return new StepBuilder("importStep", repository).tasklet(tasklet, transactionManager).build();
  }

  @Bean
  Step combtranStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      CombtranTasklet tasklet) {
    return new StepBuilder("combtranStep", repository).tasklet(tasklet, transactionManager).build();
  }

  @Bean
  Step transactionBackupStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      TransactionBackupTasklet tasklet) {
    return new StepBuilder("transactionBackupStep", repository)
        .tasklet(tasklet, transactionManager)
        .build();
  }

  @Bean
  Step categoryBalancePrintStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      CategoryBalancePrintTasklet tasklet) {
    return new StepBuilder("categoryBalancePrintStep", repository)
        .tasklet(tasklet, transactionManager)
        .build();
  }

  @Bean
  Step authorizationPurgeStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      AuthorizationPurgeTasklet tasklet) {
    return new StepBuilder("authorizationPurgeStep", repository)
        .tasklet(tasklet, transactionManager)
        .build();
  }

  @Bean
  Step transactionTypeUpdateStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      TransactionTypeUpdateTasklet tasklet) {
    return new StepBuilder("transactionTypeUpdateStep", repository)
        .tasklet(tasklet, transactionManager)
        .build();
  }
}
