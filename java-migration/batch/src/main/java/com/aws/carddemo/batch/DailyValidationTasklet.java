package com.aws.carddemo.batch;

import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.CardXrefId;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.DailyTransactionRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
class DailyValidationTasklet implements Tasklet {
  private final DailyTransactionRepository dailyTransactions;
  private final CardXrefRepository xrefs;
  private final AccountRepository accounts;

  DailyValidationTasklet(
      DailyTransactionRepository dailyTransactions,
      CardXrefRepository xrefs,
      AccountRepository accounts) {
    this.dailyTransactions = dailyTransactions;
    this.xrefs = xrefs;
    this.accounts = accounts;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext context) {
    int checked = 0;
    int missingXref = 0;
    int missingAccount = 0;
    for (var daily : dailyTransactions.findByTranIdGreaterThanOrderByTranId("")) {
      checked++;
      var xref = xrefs.findById(new CardXrefId(daily.getCardNum())).orElse(null);
      if (xref == null) {
        missingXref++;
      } else if (accounts.findById(xref.getAcctId()).isEmpty()) {
        missingAccount++;
      }
    }
    var executionContext =
        context.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
    executionContext.putInt("checkedCount", checked);
    executionContext.putInt("missingXrefCount", missingXref);
    executionContext.putInt("missingAccountCount", missingAccount);
    for (int i = 0; i < checked; i++) {
      contribution.incrementReadCount();
    }
    return RepeatStatus.FINISHED;
  }
}
