package com.aws.carddemo.batch;

import com.aws.carddemo.domain.*;
import java.math.BigDecimal;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class PostingTasklet implements Tasklet {
  private static final int INVALID_CARD = 100;
  private static final int INVALID_ACCOUNT = 101;
  private static final int OVERLIMIT = 102;
  private static final int EXPIRED = 103;

  private final DailyTransactionRepository dailyTransactions;
  private final CardXrefRepository xrefs;
  private final AccountRepository accounts;
  private final TransactionRepository transactions;
  private final TranCategoryBalanceRepository balances;
  private final DailyTransactionRejectRepository rejects;

  PostingTasklet(
      DailyTransactionRepository dailyTransactions,
      CardXrefRepository xrefs,
      AccountRepository accounts,
      TransactionRepository transactions,
      TranCategoryBalanceRepository balances,
      DailyTransactionRejectRepository rejects) {
    this.dailyTransactions = dailyTransactions;
    this.xrefs = xrefs;
    this.accounts = accounts;
    this.transactions = transactions;
    this.balances = balances;
    this.rejects = rejects;
  }

  @Override
  @Transactional
  public RepeatStatus execute(StepContribution contribution, ChunkContext context) {
    int processed = 0;
    int rejected = 0;
    for (DailyTransaction daily : dailyTransactions.findByTranIdGreaterThanOrderByTranId("")) {
      Validation failure = validate(daily);
      if (failure.code() != 0) {
        rejected++;
        writeReject(daily, failure);
      } else {
        post(daily);
        processed++;
      }
    }
    context
        .getStepContext()
        .getStepExecution()
        .getJobExecution()
        .getExecutionContext()
        .putInt("processedCount", processed);
    context
        .getStepContext()
        .getStepExecution()
        .getJobExecution()
        .getExecutionContext()
        .putInt("rejectedCount", rejected);
    for (int i = 0; i < processed + rejected; i++) {
      contribution.incrementReadCount();
    }
    for (int i = 0; i < processed; i++) {
      contribution.incrementWriteCount(1);
    }
    if (rejected > 0) {
      contribution.setExitStatus(
          new ExitStatus("COMPLETED WITH REJECTIONS", "return-code=4; rejected=" + rejected));
    }
    return RepeatStatus.FINISHED;
  }

  private Validation validate(DailyTransaction daily) {
    CardXref xref = xrefs.findById(new CardXrefId(daily.getCardNum())).orElse(null);
    if (xref == null) {
      return new Validation(INVALID_CARD, "INVALID CARD NUMBER FOUND");
    }
    Account account = accounts.findById(xref.getAcctId()).orElse(null);
    if (account == null) {
      return new Validation(INVALID_ACCOUNT, "ACCOUNT RECORD NOT FOUND");
    }
    BigDecimal temp =
        BatchSupport.value(account.getCurrCycCredit())
            .subtract(BatchSupport.value(account.getCurrCycDebit()))
            .add(BatchSupport.value(daily.getAmt()));
    Validation failure = new Validation(0, "");
    if (BatchSupport.value(account.getCreditLimit()).compareTo(temp) < 0) {
      failure = new Validation(OVERLIMIT, "OVERLIMIT TRANSACTION");
    }
    String expiration = account.getExpiraionDate();
    String originalDate = BatchSupport.dateOnly(daily.getOrigTs());
    if (expiration != null
        && !expiration.isBlank()
        && !originalDate.isBlank()
        && expiration.compareTo(originalDate) < 0) {
      failure = new Validation(EXPIRED, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
    }
    return failure;
  }

  private void post(DailyTransaction daily) {
    CardXref xref = xrefs.findById(new CardXrefId(daily.getCardNum())).orElseThrow();
    Account account = accounts.findById(xref.getAcctId()).orElseThrow();
    Transaction transaction = new Transaction();
    transaction.setTranId(daily.getTranId());
    transaction.setTypeCd(daily.getTypeCd());
    transaction.setCatCd(daily.getCatCd());
    transaction.setSource(daily.getSource());
    transaction.setTranDesc(daily.getTranDesc());
    transaction.setAmt(daily.getAmt());
    transaction.setMerchantId(daily.getMerchantId());
    transaction.setMerchantName(daily.getMerchantName());
    transaction.setMerchantCity(daily.getMerchantCity());
    transaction.setMerchantZip(daily.getMerchantZip());
    transaction.setCardNum(daily.getCardNum());
    transaction.setOrigTs(daily.getOrigTs());
    transaction.setProcTs(BatchSupport.timestamp());
    transactions.save(transaction);

    TranCategoryBalanceId id =
        new TranCategoryBalanceId(xref.getAcctId(), daily.getTypeCd(), daily.getCatCd());
    TranCategoryBalance balance =
        balances
            .findById(id)
            .orElseGet(
                () -> {
                  TranCategoryBalance created = new TranCategoryBalance();
                  created.setId(id);
                  created.setTranCatBal(BigDecimal.ZERO.setScale(2));
                  return created;
                });
    balance.setTranCatBal(
        BatchSupport.value(balance.getTranCatBal()).add(BatchSupport.value(daily.getAmt())));
    balances.save(balance);
    BigDecimal amount = BatchSupport.value(daily.getAmt());
    account.setCurrBal(BatchSupport.value(account.getCurrBal()).add(amount));
    if (amount.signum() >= 0) {
      account.setCurrCycCredit(BatchSupport.value(account.getCurrCycCredit()).add(amount));
    } else {
      account.setCurrCycDebit(BatchSupport.value(account.getCurrCycDebit()).add(amount));
    }
    accounts.save(account);
  }

  private void writeReject(DailyTransaction daily, Validation failure) {
    DailyTransactionReject reject = new DailyTransactionReject();
    reject.setTranId(daily.getTranId());
    reject.setTypeCd(daily.getTypeCd());
    reject.setCatCd(daily.getCatCd());
    reject.setSource(daily.getSource());
    reject.setTranDesc(daily.getTranDesc());
    reject.setAmt(daily.getAmt());
    reject.setMerchantId(daily.getMerchantId());
    reject.setMerchantName(daily.getMerchantName());
    reject.setMerchantCity(daily.getMerchantCity());
    reject.setMerchantZip(daily.getMerchantZip());
    reject.setCardNum(daily.getCardNum());
    reject.setOrigTs(daily.getOrigTs());
    reject.setProcTs(daily.getProcTs());
    reject.setValidationFailReason(failure.code());
    reject.setValidationFailReasonDesc(failure.description());
    reject.setValidationTrailer(String.format("%04d%-76s", failure.code(), failure.description()));
    rejects.save(reject);
  }

  private record Validation(int code, String description) {}
}
