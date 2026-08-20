package com.aws.carddemo.batch;

import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.CardXrefId;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.domain.TransactionCategory;
import com.aws.carddemo.domain.TransactionCategoryRepository;
import com.aws.carddemo.domain.TransactionRepository;
import com.aws.carddemo.domain.TransactionType;
import com.aws.carddemo.domain.TransactionTypeRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ReportTasklet implements Tasklet {
  private final TransactionRepository transactions;
  private final CardXrefRepository xrefs;
  private final TransactionTypeRepository types;
  private final TransactionCategoryRepository categories;
  private final String defaultPath;

  ReportTasklet(
      TransactionRepository transactions,
      CardXrefRepository xrefs,
      TransactionTypeRepository types,
      TransactionCategoryRepository categories,
      @Value("${carddemo.batch.report-path:target/output/transaction-report.txt}")
          String defaultPath) {
    this.transactions = transactions;
    this.xrefs = xrefs;
    this.types = types;
    this.categories = categories;
    this.defaultPath = defaultPath;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext context)
      throws IOException {
    var parameters = context.getStepContext().getStepExecution().getJobParameters();
    String start = BatchSupport.parameter(parameters, "startDate", "1900-01-01");
    String end = BatchSupport.parameter(parameters, "endDate", "2999-12-31");
    Path output = Paths.get(BatchSupport.parameter(parameters, "reportPath", defaultPath));
    Files.createDirectories(output.toAbsolutePath().getParent());
    List<Transaction> selected =
        transactions.findAll().stream()
            .filter(
                t -> {
                  String date = BatchSupport.dateOnly(t.getProcTs());
                  return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
                })
            .sorted(
                Comparator.comparing(
                        Transaction::getCardNum, Comparator.nullsFirst(String::compareTo))
                    .thenComparing(Transaction::getTranId))
            .toList();
    StringBuilder report = new StringBuilder();
    BigDecimal pageTotal = BigDecimal.ZERO;
    BigDecimal accountTotal = BigDecimal.ZERO;
    BigDecimal grandTotal = BigDecimal.ZERO;
    String currentAccount = null;
    int detailLines = 0;
    int linesOnPage = 0;
    report.append("DALYREPT                             Daily Transaction Report").append('\n');
    report
        .append("Date Range: ")
        .append(BatchSupport.pad(start, 10))
        .append(" to ")
        .append(end)
        .append('\n');
    report
        .append(
            "Transaction ID Account ID   Transaction Type    Tran Category                 Tran Source          Amount")
        .append('\n');
    report.append("-".repeat(133)).append('\n');
    for (Transaction transaction : selected) {
      CardXref xref = xrefs.findById(new CardXrefId(transaction.getCardNum())).orElse(null);
      String account = xref == null ? "" : String.valueOf(xref.getAcctId());
      if (currentAccount != null && !currentAccount.equals(account)) {
        report.append(accountTotalLine("Account Total", accountTotal)).append('\n');
        accountTotal = BigDecimal.ZERO;
      }
      if (linesOnPage == 20) {
        report.append(totalLine("Page Total", pageTotal)).append('\n');
        report.append("-".repeat(133)).append('\n');
        pageTotal = BigDecimal.ZERO;
        linesOnPage = 0;
      }
      currentAccount = account;
      String typeDescription =
          types.findById(transaction.getTypeCd()).map(TransactionType::getTypeDesc).orElse("");
      String categoryDescription =
          categories
              .findById(
                  new com.aws.carddemo.domain.TransactionCategoryId(
                      transaction.getTypeCd(), transaction.getCatCd()))
              .map(TransactionCategory::getCatTypeDesc)
              .orElse("");
      report
          .append(
              String.format(
                  "%-16s %-11s %-2s-%-15s %04d-%-29s %-10s %16s",
                  transaction.getTranId(),
                  account,
                  transaction.getTypeCd(),
                  typeDescription,
                  transaction.getCatCd(),
                  categoryDescription,
                  transaction.getSource(),
                  money(transaction.getAmt())))
          .append('\n');
      BigDecimal amount = BatchSupport.value(transaction.getAmt());
      pageTotal = pageTotal.add(amount);
      accountTotal = accountTotal.add(amount);
      grandTotal = grandTotal.add(amount);
      detailLines++;
      linesOnPage++;
    }
    if (currentAccount != null) {
      report.append(accountTotalLine("Account Total", accountTotal)).append('\n');
    }
    if (!selected.isEmpty()) {
      report.append(totalLine("Page Total", pageTotal)).append('\n');
    }
    report.append(totalLine("Grand Total", grandTotal)).append('\n');
    Files.writeString(output, report.toString());
    for (int i = 0; i < selected.size(); i++) {
      contribution.incrementReadCount();
    }
    for (int i = 0; i < detailLines; i++) {
      contribution.incrementWriteCount(1);
    }
    return RepeatStatus.FINISHED;
  }

  private String totalLine(String label, BigDecimal amount) {
    return String.format("%-11s%s%16s", label, ".".repeat(86), money(amount));
  }

  private String accountTotalLine(String label, BigDecimal amount) {
    return String.format("%-13s%s%16s", label, ".".repeat(84), money(amount));
  }

  private String money(BigDecimal amount) {
    return String.format("%,.2f", BatchSupport.value(amount));
  }
}
