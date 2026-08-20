package com.aws.carddemo.batch;

import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.CardRepository;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.CustomerRepository;
import com.aws.carddemo.domain.TransactionRepository;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
class ExportTasklet implements Tasklet {
  private final CustomerRepository customers;
  private final AccountRepository accounts;
  private final CardXrefRepository xrefs;
  private final TransactionRepository transactions;
  private final CardRepository cards;

  ExportTasklet(
      CustomerRepository customers,
      AccountRepository accounts,
      CardXrefRepository xrefs,
      TransactionRepository transactions,
      CardRepository cards) {
    this.customers = customers;
    this.accounts = accounts;
    this.xrefs = xrefs;
    this.transactions = transactions;
    this.cards = cards;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext context)
      throws IOException {
    String path =
        BatchSupport.parameter(
            context.getStepContext().getStepExecution().getJobParameters(),
            "exportPath",
            "target/output/carddemo-export.dat");
    Path output = Path.of(path).toAbsolutePath();
    Files.createDirectories(output.getParent());
    int sequence = 0;
    int customerCount = 0;
    int accountCount = 0;
    int xrefCount = 0;
    int transactionCount = 0;
    int cardCount = 0;
    try (BufferedWriter writer =
        Files.newBufferedWriter(
            output,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE)) {
      for (var customer :
          customers.findAll().stream()
              .sorted((a, b) -> a.getCustId().compareTo(b.getCustId()))
              .toList()) {
        writer.write(ExportCodec.customer(++sequence, customer));
        writer.newLine();
        customerCount++;
      }
      for (var account :
          accounts.findAll().stream()
              .sorted((a, b) -> a.getAcctId().compareTo(b.getAcctId()))
              .toList()) {
        writer.write(ExportCodec.account(++sequence, account));
        writer.newLine();
        accountCount++;
      }
      for (var xref :
          xrefs.findAll().stream()
              .sorted((a, b) -> a.getId().getCardNum().compareTo(b.getId().getCardNum()))
              .toList()) {
        writer.write(ExportCodec.xref(++sequence, xref));
        writer.newLine();
        xrefCount++;
      }
      for (var transaction :
          transactions.findAll().stream()
              .sorted((a, b) -> a.getTranId().compareTo(b.getTranId()))
              .toList()) {
        writer.write(ExportCodec.transaction(++sequence, transaction));
        writer.newLine();
        transactionCount++;
      }
      for (var card :
          cards.findAll().stream()
              .sorted((a, b) -> a.getCardNum().compareTo(b.getCardNum()))
              .toList()) {
        writer.write(ExportCodec.card(++sequence, card));
        writer.newLine();
        cardCount++;
      }
    }
    var executionContext =
        context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
    executionContext.putInt("customerRecordsExported", customerCount);
    executionContext.putInt("accountRecordsExported", accountCount);
    executionContext.putInt("xrefRecordsExported", xrefCount);
    executionContext.putInt("transactionRecordsExported", transactionCount);
    executionContext.putInt("cardRecordsExported", cardCount);
    executionContext.putInt("totalRecordsExported", sequence);
    contribution.incrementWriteCount(sequence);
    return RepeatStatus.FINISHED;
  }
}
