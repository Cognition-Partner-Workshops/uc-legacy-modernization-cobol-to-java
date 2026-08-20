package com.aws.carddemo.batch;

import com.aws.carddemo.common.CobolDecimal;
import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.CardXref;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.DisclosureGroup;
import com.aws.carddemo.domain.DisclosureGroupId;
import com.aws.carddemo.domain.DisclosureGroupRepository;
import com.aws.carddemo.domain.TranCategoryBalance;
import com.aws.carddemo.domain.TranCategoryBalanceRepository;
import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.domain.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class InterestTasklet implements Tasklet {
  private final TranCategoryBalanceRepository balances;
  private final AccountRepository accounts;
  private final CardXrefRepository xrefs;
  private final DisclosureGroupRepository disclosureGroups;
  private final TransactionRepository transactions;

  InterestTasklet(
      TranCategoryBalanceRepository balances,
      AccountRepository accounts,
      CardXrefRepository xrefs,
      DisclosureGroupRepository disclosureGroups,
      TransactionRepository transactions) {
    this.balances = balances;
    this.accounts = accounts;
    this.xrefs = xrefs;
    this.disclosureGroups = disclosureGroups;
    this.transactions = transactions;
  }

  @Override
  @Transactional
  public RepeatStatus execute(StepContribution contribution, ChunkContext context) {
    String runDate =
        BatchSupport.parameter(
            context.getStepContext().getStepExecution().getJobParameters(),
            "runDate",
            LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    List<TranCategoryBalance> rows =
        balances.findAll().stream()
            .sorted(
                Comparator.comparing((TranCategoryBalance row) -> row.getId().getAcctId())
                    .thenComparing(row -> row.getId().getTypeCd())
                    .thenComparing(row -> row.getId().getCatCd()))
            .toList();
    Map<Long, BigDecimal> totals = new HashMap<>();
    int suffix = 0;
    for (TranCategoryBalance row : rows) {
      Account account = accounts.findById(row.getId().getAcctId()).orElseThrow();
      DisclosureGroup group =
          disclosureGroups
              .findById(
                  new DisclosureGroupId(
                      account.getGroupId(), row.getId().getTypeCd(), row.getId().getCatCd()))
              .orElseGet(
                  () ->
                      disclosureGroups
                          .findById(
                              new DisclosureGroupId(
                                  "DEFAULT", row.getId().getTypeCd(), row.getId().getCatCd()))
                          .orElse(null));
      if (group != null && BatchSupport.value(group.getIntRate()).signum() != 0) {
        BigDecimal interest =
            CobolDecimal.rounded(
                BatchSupport.value(row.getTranCatBal())
                    .multiply(BatchSupport.value(group.getIntRate()))
                    .divide(BigDecimal.valueOf(1200), 8, java.math.RoundingMode.HALF_UP),
                2);
        totals.merge(row.getId().getAcctId(), interest, BigDecimal::add);
        suffix++;
        writeInterestTransaction(runDate, suffix, account, interest);
        computeFeesNoOp();
      }
      Long nextAccount = nextAccount(rows, row);
      if (nextAccount == null || !nextAccount.equals(row.getId().getAcctId())) {
        Account current = accounts.findById(row.getId().getAcctId()).orElseThrow();
        BigDecimal total = totals.getOrDefault(row.getId().getAcctId(), BigDecimal.ZERO);
        current.setCurrBal(BatchSupport.value(current.getCurrBal()).add(total));
        current.setCurrCycCredit(BigDecimal.ZERO.setScale(2));
        current.setCurrCycDebit(BigDecimal.ZERO.setScale(2));
        accounts.save(current);
      }
    }
    contribution.incrementWriteCount(suffix);
    return RepeatStatus.FINISHED;
  }

  private Long nextAccount(List<TranCategoryBalance> rows, TranCategoryBalance row) {
    int index = rows.indexOf(row);
    return index + 1 < rows.size() ? rows.get(index + 1).getId().getAcctId() : null;
  }

  private void writeInterestTransaction(
      String runDate, int suffix, Account account, BigDecimal amount) {
    CardXref xref = xrefs.findByAcctId(account.getAcctId()).stream().findFirst().orElse(null);
    String now = BatchSupport.timestamp();
    Transaction transaction = new Transaction();
    transaction.setTranId(runDate.replace("-", "") + String.format("%06d", suffix));
    transaction.setTypeCd("01");
    transaction.setCatCd(5);
    transaction.setSource("System");
    transaction.setTranDesc("Int. for a/c " + account.getAcctId());
    transaction.setAmt(amount);
    transaction.setMerchantId(0);
    transaction.setMerchantName("");
    transaction.setMerchantCity("");
    transaction.setMerchantZip("");
    transaction.setCardNum(xref == null ? "" : xref.getId().getCardNum());
    transaction.setOrigTs(now);
    transaction.setProcTs(now);
    transactions.save(transaction);
  }

  private void computeFeesNoOp() {
    // COBOL paragraph 1400-COMPUTE-FEES is intentionally empty.
  }
}
