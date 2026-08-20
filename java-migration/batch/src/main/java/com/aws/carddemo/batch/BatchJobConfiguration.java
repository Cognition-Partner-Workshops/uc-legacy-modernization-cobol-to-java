package com.aws.carddemo.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration {
  @Bean
  JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(
      org.springframework.batch.core.configuration.JobRegistry jobRegistry) {
    JobRegistryBeanPostProcessor processor = new JobRegistryBeanPostProcessor();
    processor.setJobRegistry(jobRegistry);
    return processor;
  }

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
}
