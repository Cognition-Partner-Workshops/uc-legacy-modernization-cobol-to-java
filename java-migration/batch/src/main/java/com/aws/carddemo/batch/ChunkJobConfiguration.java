package com.aws.carddemo.batch;

import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.Card;
import com.aws.carddemo.domain.CardRepository;
import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.Customer;
import com.aws.carddemo.domain.CustomerRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
class ChunkJobConfiguration {
  @Bean
  Job accountFilePrintJob(
      JobRepository repository, @Qualifier("accountFilePrintStep") Step accountFilePrintStep) {
    return new JobBuilder("accountFilePrintJob", repository).start(accountFilePrintStep).build();
  }

  @Bean
  Job cardFilePrintJob(
      JobRepository repository, @Qualifier("cardFilePrintStep") Step cardFilePrintStep) {
    return new JobBuilder("cardFilePrintJob", repository).start(cardFilePrintStep).build();
  }

  @Bean
  Job xrefFilePrintJob(
      JobRepository repository, @Qualifier("xrefFilePrintStep") Step xrefFilePrintStep) {
    return new JobBuilder("xrefFilePrintJob", repository).start(xrefFilePrintStep).build();
  }

  @Bean
  Job customerFilePrintJob(
      JobRepository repository, @Qualifier("customerFilePrintStep") Step customerFilePrintStep) {
    return new JobBuilder("customerFilePrintJob", repository).start(customerFilePrintStep).build();
  }

  @Bean
  Step accountFilePrintStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      @Qualifier("accountFileReader") ItemReader<Account> reader,
      @Qualifier("accountFileProcessor") ItemProcessor<Account, String> processor,
      @Qualifier("accountFileWriter") ItemWriter<String> writer) {
    return new StepBuilder("accountFilePrintStep", repository)
        .<Account, String>chunk(10, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  @Bean
  Step cardFilePrintStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      @Qualifier("cardFileReader") ItemReader<Card> reader,
      @Qualifier("cardFileProcessor") ItemProcessor<Card, String> processor,
      @Qualifier("cardFileWriter") ItemWriter<String> writer) {
    return new StepBuilder("cardFilePrintStep", repository)
        .<Card, String>chunk(10, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  @Bean
  Step xrefFilePrintStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      @Qualifier("xrefFileReader") ItemReader<CardXref> reader,
      @Qualifier("xrefFileProcessor") ItemProcessor<CardXref, String> processor,
      @Qualifier("xrefFileWriter") ItemWriter<String> writer) {
    return new StepBuilder("xrefFilePrintStep", repository)
        .<CardXref, String>chunk(10, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  @Bean
  Step customerFilePrintStep(
      JobRepository repository,
      PlatformTransactionManager transactionManager,
      @Qualifier("customerFileReader") ItemReader<Customer> reader,
      @Qualifier("customerFileProcessor") ItemProcessor<Customer, String> processor,
      @Qualifier("customerFileWriter") ItemWriter<String> writer) {
    return new StepBuilder("customerFilePrintStep", repository)
        .<Customer, String>chunk(10, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  @Bean
  @StepScope
  ItemReader<Account> accountFileReader(AccountRepository accounts) {
    return new ListItemReader<>(
        accounts.findAll().stream()
            .sorted((a, b) -> a.getAcctId().compareTo(b.getAcctId()))
            .toList());
  }

  @Bean
  @StepScope
  ItemReader<Card> cardFileReader(CardRepository cards) {
    return new ListItemReader<>(
        cards.findAll().stream()
            .sorted((a, b) -> a.getCardNum().compareTo(b.getCardNum()))
            .toList());
  }

  @Bean
  @StepScope
  ItemReader<CardXref> xrefFileReader(CardXrefRepository xrefs) {
    return new ListItemReader<>(
        xrefs.findAll().stream()
            .sorted((a, b) -> a.getId().getCardNum().compareTo(b.getId().getCardNum()))
            .toList());
  }

  @Bean
  @StepScope
  ItemReader<Customer> customerFileReader(CustomerRepository customers) {
    return new ListItemReader<>(
        customers.findAll().stream()
            .sorted((a, b) -> a.getCustId().compareTo(b.getCustId()))
            .toList());
  }

  @Bean
  @StepScope
  ItemProcessor<Account, String> accountFileProcessor() {
    return account ->
        String.join(
            System.lineSeparator(),
            List.of(
                String.format("ACCT-ID                 :%s", account.getAcctId()),
                String.format("ACCT-ACTIVE-STATUS      :%s", text(account.getActiveStatus())),
                String.format("ACCT-CURR-BAL           :%s", money(account.getCurrBal())),
                String.format("ACCT-CREDIT-LIMIT       :%s", money(account.getCreditLimit())),
                String.format("ACCT-CASH-CREDIT-LIMIT  :%s", money(account.getCashCreditLimit())),
                String.format("ACCT-OPEN-DATE          :%s", text(account.getOpenDate())),
                String.format("ACCT-EXPIRAION-DATE     :%s", text(account.getExpiraionDate())),
                String.format("ACCT-REISSUE-DATE       :%s", text(account.getReissueDate())),
                String.format("ACCT-CURR-CYC-CREDIT    :%s", money(account.getCurrCycCredit())),
                String.format("ACCT-CURR-CYC-DEBIT     :%s", money(account.getCurrCycDebit())),
                String.format("ACCT-GROUP-ID           :%s", text(account.getGroupId())),
                "-------------------------------------------------"));
  }

  @Bean
  @StepScope
  ItemProcessor<Card, String> cardFileProcessor() {
    return card ->
        String.format(
            "CARD-RECORD %s %s %s %s %s %s",
            text(card.getCardNum()),
            card.getAcctId(),
            card.getCvvCd(),
            text(card.getEmbossedName()),
            text(card.getExpiraionDate()),
            text(card.getActiveStatus()));
  }

  @Bean
  @StepScope
  ItemProcessor<CardXref, String> xrefFileProcessor() {
    return xref ->
        String.format(
            "CARD-XREF-RECORD %s %s %s",
            text(xref.getId().getCardNum()), xref.getCustId(), xref.getAcctId());
  }

  @Bean
  @StepScope
  ItemProcessor<Customer, String> customerFileProcessor() {
    return customer ->
        String.format(
            "CUSTOMER-RECORD %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s",
            customer.getCustId(),
            text(customer.getFirstName()),
            text(customer.getMiddleName()),
            text(customer.getLastName()),
            text(customer.getAddrLine1()),
            text(customer.getAddrLine2()),
            text(customer.getAddrLine3()),
            text(customer.getAddrStateCd()),
            text(customer.getAddrCountryCd()),
            text(customer.getAddrZip()),
            text(customer.getPhoneNum1()),
            text(customer.getPhoneNum2()),
            customer.getSsn(),
            text(customer.getGovtIssuedId()),
            text(customer.getDobYyyyMmDd()),
            text(customer.getEftAccountId()),
            text(customer.getPriCardHolderInd()),
            customer.getFicoCreditScore());
  }

  @Bean
  @StepScope
  FlatFileItemWriter<String> accountFileWriter(
      @Value("#{jobParameters['outputPath'] ?: 'target/output/account-file.txt'}")
          String outputPath) {
    return writer(
        "accountFileWriter",
        outputPath,
        "START OF EXECUTION OF PROGRAM CBACT01C",
        "END OF EXECUTION OF PROGRAM CBACT01C");
  }

  @Bean
  @StepScope
  FlatFileItemWriter<String> cardFileWriter(
      @Value("#{jobParameters['outputPath'] ?: 'target/output/card-file.txt'}") String outputPath) {
    return writer(
        "cardFileWriter",
        outputPath,
        "START OF EXECUTION OF PROGRAM CBACT02C",
        "END OF EXECUTION OF PROGRAM CBACT02C");
  }

  @Bean
  @StepScope
  FlatFileItemWriter<String> xrefFileWriter(
      @Value("#{jobParameters['outputPath'] ?: 'target/output/xref-file.txt'}") String outputPath) {
    return writer(
        "xrefFileWriter",
        outputPath,
        "START OF EXECUTION OF PROGRAM CBACT03C",
        "END OF EXECUTION OF PROGRAM CBACT03C");
  }

  @Bean
  @StepScope
  FlatFileItemWriter<String> customerFileWriter(
      @Value("#{jobParameters['outputPath'] ?: 'target/output/customer-file.txt'}")
          String outputPath) {
    return writer(
        "customerFileWriter",
        outputPath,
        "START OF EXECUTION OF PROGRAM CBCUS01C",
        "END OF EXECUTION OF PROGRAM CBCUS01C");
  }

  private FlatFileItemWriter<String> writer(
      String name, String outputPath, String header, String footer) {
    try {
      Path path = Path.of(outputPath).toAbsolutePath();
      Files.createDirectories(path.getParent());
      return new FlatFileItemWriterBuilder<String>()
          .name(name)
          .resource(new org.springframework.core.io.FileSystemResource(path))
          .lineAggregator(item -> item)
          .headerCallback(writer -> writer.write(header))
          .footerCallback(writer -> writer.write(footer))
          .shouldDeleteIfExists(true)
          .build();
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create output directory " + outputPath, e);
    }
  }

  private static String text(String value) {
    return value == null ? "" : value.trim();
  }

  private static String money(java.math.BigDecimal value) {
    return BatchSupport.value(value).toPlainString();
  }
}
