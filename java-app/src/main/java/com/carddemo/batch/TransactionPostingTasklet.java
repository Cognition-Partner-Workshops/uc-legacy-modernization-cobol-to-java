package com.carddemo.batch;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.DailyTransaction;
import com.carddemo.model.RejectedTransaction;
import com.carddemo.model.Transaction;
import com.carddemo.model.TransactionCategoryBalance;
import com.carddemo.model.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.RejectedTransactionRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.DateTimeUtil;
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

@Component
public class TransactionPostingTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingTasklet.class);

    private final DailyTransactionRepository dailyTransactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryBalanceRepository tcatBalRepository;
    private final RejectedTransactionRepository rejectedTransactionRepository;

    public TransactionPostingTasklet(DailyTransactionRepository dailyTransactionRepository,
                                     CardXrefRepository cardXrefRepository,
                                     AccountRepository accountRepository,
                                     TransactionRepository transactionRepository,
                                     TransactionCategoryBalanceRepository tcatBalRepository,
                                     RejectedTransactionRepository rejectedTransactionRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.tcatBalRepository = tcatBalRepository;
        this.rejectedTransactionRepository = rejectedTransactionRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<DailyTransaction> dailyTransactions = dailyTransactionRepository.findAllByOrderByTranIdAsc();
        log.info("Processing {} daily transactions", dailyTransactions.size());

        int processed = 0;
        int rejected = 0;

        for (DailyTransaction dt : dailyTransactions) {
            try {
                // Look up card in CardXref (CBTRN02C lines 380-392)
                Optional<CardXref> xrefOpt = cardXrefRepository.findById(dt.getCardNum());
                if (xrefOpt.isEmpty()) {
                    rejectTransaction(dt, 100, "Card number not found in cross-reference");
                    rejected++;
                    continue;
                }

                CardXref xref = xrefOpt.get();

                // Look up account (CBTRN02C lines 393-422)
                Optional<Account> accountOpt = accountRepository.findById(xref.getAcctId());
                if (accountOpt.isEmpty()) {
                    rejectTransaction(dt, 101, "Account not found");
                    rejected++;
                    continue;
                }

                Account account = accountOpt.get();

                // Check credit limit (CBTRN02C lines 403-413)
                BigDecimal newBalance = account.getCurrBal().add(dt.getAmount());
                if (newBalance.compareTo(account.getCreditLimit()) > 0) {
                    rejectTransaction(dt, 102, "Transaction exceeds credit limit");
                    rejected++;
                    continue;
                }

                // Check account expiration (CBTRN02C lines 414-420)
                if (DateTimeUtil.isDateExpired(account.getExpirationDate())) {
                    rejectTransaction(dt, 103, "Account expired");
                    rejected++;
                    continue;
                }

                // Post valid transaction (CBTRN02C lines 424-443)
                String timestamp = DateTimeUtil.generateTransactionTimestamp();
                Transaction transaction = Transaction.builder()
                        .tranId(dt.getTranId())
                        .typeCd(dt.getTypeCd())
                        .catCd(dt.getCatCd())
                        .source(dt.getSource())
                        .description(dt.getDescription())
                        .amount(dt.getAmount())
                        .merchantId(dt.getMerchantId())
                        .merchantName(dt.getMerchantName())
                        .merchantCity(dt.getMerchantCity())
                        .merchantZip(dt.getMerchantZip())
                        .cardNum(dt.getCardNum())
                        .origTimestamp(dt.getOrigTimestamp())
                        .procTimestamp(timestamp)
                        .build();
                transactionRepository.save(transaction);

                // Update category balance (CBTRN02C lines 467-542)
                updateCategoryBalance(xref.getAcctId(), dt.getTypeCd(), dt.getCatCd(), dt.getAmount());

                // Update account balances (CBTRN02C lines 545-560)
                account.setCurrBal(newBalance);
                if (dt.getAmount().signum() >= 0) {
                    account.setCurrCycDebit(account.getCurrCycDebit().add(dt.getAmount()));
                } else {
                    account.setCurrCycCredit(account.getCurrCycCredit().add(dt.getAmount().abs()));
                }
                accountRepository.save(account);

                processed++;
            } catch (Exception e) {
                log.error("Error processing transaction {}: {}", dt.getTranId(), e.getMessage());
                rejectTransaction(dt, 999, "Unexpected error: " + e.getMessage());
                rejected++;
            }
        }

        log.info("Transaction posting complete. Processed: {}, Rejected: {}", processed, rejected);
        return RepeatStatus.FINISHED;
    }

    private void rejectTransaction(DailyTransaction dt, int reasonCode, String reason) {
        RejectedTransaction rejected = RejectedTransaction.builder()
                .tranId(dt.getTranId())
                .typeCd(dt.getTypeCd())
                .catCd(dt.getCatCd())
                .source(dt.getSource())
                .description(dt.getDescription())
                .amount(dt.getAmount())
                .cardNum(dt.getCardNum())
                .rejectReasonCd(reasonCode)
                .rejectReason(reason)
                .build();
        rejectedTransactionRepository.save(rejected);
    }

    private void updateCategoryBalance(Long acctId, String typeCd, Integer catCd, BigDecimal amount) {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(acctId, typeCd, catCd);
        TransactionCategoryBalance tcatBal = tcatBalRepository.findById(id)
                .orElse(TransactionCategoryBalance.builder()
                        .acctId(acctId)
                        .typeCd(typeCd)
                        .catCd(catCd)
                        .balance(BigDecimal.ZERO)
                        .build());
        tcatBal.setBalance(tcatBal.getBalance().add(amount));
        tcatBalRepository.save(tcatBal);
    }
}
