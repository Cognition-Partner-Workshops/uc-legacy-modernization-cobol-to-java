package com.aws.carddemo.batch;

import com.aws.carddemo.domain.TranCategoryBalanceRepository;
import com.aws.carddemo.domain.TransactionRepository;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
class TransactionBackupTasklet implements Tasklet {
  private final TransactionRepository transactions;

  TransactionBackupTasklet(TransactionRepository transactions) {
    this.transactions = transactions;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext context)
      throws Exception {
    String path =
        BatchSupport.parameter(
            context.getStepContext().getStepExecution().getJobParameters(),
            "backupPath",
            "target/output/transaction-backup.dat");
    Path output = Path.of(path).toAbsolutePath();
    Files.createDirectories(output.getParent());
    int count = 0;
    try (BufferedWriter writer =
        Files.newBufferedWriter(
            output,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE)) {
      for (var transaction : transactions.findAllByOrderByTranIdAsc()) {
        writer.write(ExportCodec.transaction(++count, transaction));
        writer.newLine();
      }
    }
    contribution.incrementWriteCount(count);
    return RepeatStatus.FINISHED;
  }
}

@Component
class CategoryBalancePrintTasklet implements Tasklet {
  private final TranCategoryBalanceRepository balances;

  CategoryBalancePrintTasklet(TranCategoryBalanceRepository balances) {
    this.balances = balances;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext context)
      throws Exception {
    String path =
        BatchSupport.parameter(
            context.getStepContext().getStepExecution().getJobParameters(),
            "outputPath",
            "target/output/category-balances.txt");
    Path output = Path.of(path).toAbsolutePath();
    Files.createDirectories(output.getParent());
    try (BufferedWriter writer = Files.newBufferedWriter(output)) {
      for (var balance :
          balances.findAll().stream()
              .sorted(
                  (a, b) -> {
                    int account = a.getId().getAcctId().compareTo(b.getId().getAcctId());
                    if (account != 0) return account;
                    int type = a.getId().getTypeCd().compareTo(b.getId().getTypeCd());
                    return type != 0 ? type : a.getId().getCatCd().compareTo(b.getId().getCatCd());
                  })
              .toList()) {
        writer.write(
            String.format(
                "%d %s %d %s",
                balance.getId().getAcctId(),
                balance.getId().getTypeCd(),
                balance.getId().getCatCd(),
                balance.getTranCatBal()));
        writer.newLine();
      }
    }
    int count = (int) balances.count();
    contribution.incrementWriteCount(count);
    return RepeatStatus.FINISHED;
  }
}
