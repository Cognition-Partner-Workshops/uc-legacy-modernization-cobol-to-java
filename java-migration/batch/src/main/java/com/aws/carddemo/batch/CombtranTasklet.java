package com.aws.carddemo.batch;

import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.domain.TransactionRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class CombtranTasklet implements Tasklet {
  private final TransactionRepository transactions;

  CombtranTasklet(TransactionRepository transactions) {
    this.transactions = transactions;
  }

  @Override
  @Transactional
  public RepeatStatus execute(StepContribution contribution, ChunkContext context)
      throws IOException {
    var parameters = context.getStepContext().getStepExecution().getJobParameters();
    String backupPath =
        BatchSupport.parameter(parameters, "backupPath", "target/input/transaction-backup.dat");
    String systemPath =
        BatchSupport.parameter(parameters, "systemPath", "target/input/system-transactions.dat");
    List<Transaction> merged = new ArrayList<>();
    read(Path.of(backupPath), merged);
    read(Path.of(systemPath), merged);
    merged.sort(Comparator.comparing(Transaction::getTranId));
    transactions.saveAll(merged);
    contribution.incrementWriteCount(merged.size());
    context
        .getStepContext()
        .getStepExecution()
        .getJobExecution()
        .getExecutionContext()
        .putInt("mergedCount", merged.size());
    return RepeatStatus.FINISHED;
  }

  private void read(Path path, List<Transaction> target) throws IOException {
    if (!Files.exists(path)) {
      return;
    }
    for (String line : Files.readAllLines(path)) {
      if (line.isBlank()) {
        continue;
      }
      ExportCodec.ExportRecord record = ExportCodec.decode(line);
      if (record.type() != 'T') {
        throw new IllegalArgumentException("COMBTRAN input contains non-transaction record");
      }
      target.add(toTransaction(record.fields()));
    }
  }

  private Transaction toTransaction(List<String> fields) {
    if (fields.size() != 13) {
      throw new IllegalArgumentException("Transaction export record has an invalid field count");
    }
    Transaction row = new Transaction();
    row.setTranId(fields.get(0));
    row.setTypeCd(fields.get(1));
    row.setCatCd(integer(fields.get(2)));
    row.setSource(fields.get(3));
    row.setTranDesc(fields.get(4));
    row.setAmt(decimal(fields.get(5)));
    row.setMerchantId(integer(fields.get(6)));
    row.setMerchantName(fields.get(7));
    row.setMerchantCity(fields.get(8));
    row.setMerchantZip(fields.get(9));
    row.setCardNum(fields.get(10));
    row.setOrigTs(fields.get(11));
    row.setProcTs(fields.get(12));
    return row;
  }

  private Integer integer(String value) {
    return value.isBlank() ? null : Integer.valueOf(value);
  }

  private java.math.BigDecimal decimal(String value) {
    return value.isBlank() ? null : new java.math.BigDecimal(value);
  }
}
