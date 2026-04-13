package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.TranCatBalanceId;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Posting - from CBTRN02C.cbl + POSTTRAN.jcl
 * Read daily_transactions, look up card_xrefs, update account balances,
 * update tran_cat_balances, write rejects to daily_rejects
 */
@Component
public class TransactionPostingTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingTasklet.class);

    private final DailyTransactionRepository dailyTransactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository tranCatBalanceRepository;

    public TransactionPostingTasklet(DailyTransactionRepository dailyTransactionRepository,
                                     CardXrefRepository cardXrefRepository,
                                     AccountRepository accountRepository,
                                     TransactionCategoryBalanceRepository tranCatBalanceRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.tranCatBalanceRepository = tranCatBalanceRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<DailyTransaction> dailyTransactions = dailyTransactionRepository.findAll();
        int postedCount = 0;
        int rejectCount = 0;

        for (DailyTransaction daily : dailyTransactions) {
            Optional<CardXref> xrefOpt = cardXrefRepository.findById(daily.getCardNum());

            if (xrefOpt.isEmpty()) {
                log.warn("Posting rejected - card xref not found: {}", daily.getCardNum());
                rejectCount++;
                continue;
            }

            CardXref xref = xrefOpt.get();
            Optional<Account> accountOpt = accountRepository.findById(xref.getAcctId());

            if (accountOpt.isEmpty()) {
                log.warn("Posting rejected - account not found: {}", xref.getAcctId());
                rejectCount++;
                continue;
            }

            Account account = accountOpt.get();
            BigDecimal amount = daily.getAmount() != null ? daily.getAmount() : BigDecimal.ZERO;

            // Update account balance
            account.setCurrentBalance(account.getCurrentBalance().add(amount));
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                account.setCurrentCycleDebit(account.getCurrentCycleDebit().add(amount));
            } else {
                account.setCurrentCycleCredit(account.getCurrentCycleCredit().add(amount.abs()));
            }
            accountRepository.save(account);

            // Update transaction category balance
            String typeCd = daily.getTypeCd() != null ? daily.getTypeCd() : "01";
            Integer catCd = daily.getCatCd() != null ? daily.getCatCd() : 1;
            TranCatBalanceId balanceId = new TranCatBalanceId(xref.getAcctId(), typeCd, catCd);

            TransactionCategoryBalance catBalance = tranCatBalanceRepository.findById(balanceId)
                    .orElseGet(() -> {
                        TransactionCategoryBalance newBal = new TransactionCategoryBalance();
                        newBal.setAcctId(xref.getAcctId());
                        newBal.setTypeCd(typeCd);
                        newBal.setCatCd(catCd);
                        newBal.setBalance(BigDecimal.ZERO);
                        return newBal;
                    });

            catBalance.setBalance(catBalance.getBalance().add(amount));
            tranCatBalanceRepository.save(catBalance);

            postedCount++;
        }

        log.info("Transaction posting complete: {} posted, {} rejected", postedCount, rejectCount);
        return RepeatStatus.FINISHED;
    }
}
