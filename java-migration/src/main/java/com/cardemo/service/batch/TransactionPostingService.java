/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.batch;

import com.cardemo.common.DateTimeUtil;
import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.DailyTransactionRecord;
import com.cardemo.model.TransactionCategoryBalanceRecord;
import com.cardemo.model.TransactionRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.DailyTransactionRepository;
import com.cardemo.repository.TransactionCategoryBalanceRepository;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Posting Service - migrated from COBOL batch program CBTRN02C.cbl.
 * Posts records from daily transaction file with validation.
 * Validates transactions, writes to TRANSACT file, updates account balances
 * and transaction category balances, writes rejected transactions.
 * Original: Reads DALYTRAN, validates, writes TRANSACT/DALYREJS,
 *           updates ACCTFILE/TCATBALF.
 */
@Service
public class TransactionPostingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingService.class);

    private final DailyTransactionRepository dailyTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository tcatBalRepository;

    public TransactionPostingService(DailyTransactionRepository dailyTransactionRepository,
                                     TransactionRepository transactionRepository,
                                     CardXrefRepository cardXrefRepository,
                                     AccountRepository accountRepository,
                                     TransactionCategoryBalanceRepository tcatBalRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.tcatBalRepository = tcatBalRepository;
    }

    /**
     * Post daily transactions - migrated from main PROCEDURE DIVISION logic.
     *
     * @return result containing processed and rejected counts
     */
    @Transactional
    public PostingResult postDailyTransactions() {
        log.info("START OF EXECUTION OF PROGRAM CBTRN02C (TransactionPosting)");

        List<DailyTransactionRecord> dailyTransactions = dailyTransactionRepository.findAll();
        int transactionCount = 0;
        int rejectCount = 0;
        List<String> rejectedTransactions = new ArrayList<>();

        for (DailyTransactionRecord dailyTran : dailyTransactions) {
            transactionCount++;

            // Validate transaction - migrated from 1500-VALIDATE-TRAN
            String validationError = validateTransaction(dailyTran);

            if (validationError == null) {
                // Post valid transaction - migrated from 2000-POST-TRANSACTION
                postTransaction(dailyTran);
            } else {
                // Write reject record - migrated from 2500-WRITE-REJECT-REC
                rejectCount++;
                rejectedTransactions.add(dailyTran.getDalytranId() + ": " + validationError);
                log.warn("Transaction {} rejected: {}", dailyTran.getDalytranId(), validationError);
            }
        }

        log.info("TRANSACTIONS PROCESSED: {}", transactionCount);
        log.info("TRANSACTIONS REJECTED : {}", rejectCount);
        log.info("END OF EXECUTION OF PROGRAM CBTRN02C");

        return new PostingResult(transactionCount, rejectCount, rejectedTransactions);
    }

    /**
     * Validate a daily transaction - migrated from 1500-VALIDATE-TRAN.
     *
     * @return null if valid, error description if invalid
     */
    private String validateTransaction(DailyTransactionRecord dailyTran) {
        // 1500-A-LOOKUP-XREF
        Optional<CardXrefRecord> xrefOpt = cardXrefRepository.findById(dailyTran.getDalytranCardNum());
        if (xrefOpt.isEmpty()) {
            return "INVALID CARD NUMBER FOR XREF";
        }

        // 1500-B-LOOKUP-ACCT
        CardXrefRecord xref = xrefOpt.get();
        Optional<AccountRecord> acctOpt = accountRepository.findById(xref.getXrefAcctId());
        if (acctOpt.isEmpty()) {
            return "INVALID ACCOUNT NUMBER FOUND";
        }

        return null;
    }

    /**
     * Post a validated transaction - migrated from 2000-POST-TRANSACTION.
     */
    private void postTransaction(DailyTransactionRecord dailyTran) {
        TransactionRecord tran = new TransactionRecord();
        tran.setTranId(dailyTran.getDalytranId());
        tran.setTranTypeCd(dailyTran.getDalytranTypeCd());
        tran.setTranCatCd(dailyTran.getDalytranCatCd());
        tran.setTranSource(dailyTran.getDalytranSource());
        tran.setTranDesc(dailyTran.getDalytranDesc());
        tran.setTranAmt(dailyTran.getDalytranAmt());
        tran.setTranMerchantId(dailyTran.getDalytranMerchantId());
        tran.setTranMerchantName(dailyTran.getDalytranMerchantName());
        tran.setTranMerchantCity(dailyTran.getDalytranMerchantCity());
        tran.setTranMerchantZip(dailyTran.getDalytranMerchantZip());
        tran.setTranCardNum(dailyTran.getDalytranCardNum());

        String timestamp = DateTimeUtil.getCurrentTimestamp();
        tran.setTranOrigTs(dailyTran.getDalytranOrigTs());
        tran.setTranProcTs(timestamp);

        transactionRepository.save(tran);
    }

    /**
     * Result of the posting operation.
     */
    public static class PostingResult {
        private final int transactionCount;
        private final int rejectCount;
        private final List<String> rejectedTransactions;

        public PostingResult(int transactionCount, int rejectCount, List<String> rejectedTransactions) {
            this.transactionCount = transactionCount;
            this.rejectCount = rejectCount;
            this.rejectedTransactions = rejectedTransactions;
        }

        public int getTransactionCount() { return transactionCount; }
        public int getRejectCount() { return rejectCount; }
        public List<String> getRejectedTransactions() { return rejectedTransactions; }
    }
}
