package com.cardemo.service.batch;

import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.DailyTransactionRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.DailyTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Daily Transaction Posting Service - migrated from COBOL batch program CBTRN01C.cbl.
 * Posts records from daily transaction file by looking up cross-reference and account data.
 * Original: Reads DALYTRAN, looks up XREFFILE and ACCTFILE, writes to TRANSACT.
 */
@Service
public class DailyTransactionPostingService {

    private static final Logger log = LoggerFactory.getLogger(DailyTransactionPostingService.class);

    private final DailyTransactionRepository dailyTransactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;

    public DailyTransactionPostingService(DailyTransactionRepository dailyTransactionRepository,
                                          CardXrefRepository cardXrefRepository,
                                          AccountRepository accountRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Process daily transactions - migrated from main PROCEDURE DIVISION logic.
     * For each daily transaction, looks up the card cross-reference and account.
     *
     * @return number of transactions processed
     */
    public int processDailyTransactions() {
        log.info("START OF EXECUTION OF PROGRAM CBTRN01C (DailyTransactionPosting)");

        List<DailyTransactionRecord> dailyTransactions = dailyTransactionRepository.findAll();
        int count = 0;

        for (DailyTransactionRecord dailyTran : dailyTransactions) {
            count++;
            log.info("Processing daily transaction: {}", dailyTran);

            // Lookup cross-reference by card number
            String cardNum = dailyTran.getDalytranCardNum();
            Optional<CardXrefRecord> xrefOpt = cardXrefRepository.findById(cardNum);

            if (xrefOpt.isEmpty()) {
                log.warn("CARD NUMBER {} COULD NOT BE VERIFIED. SKIPPING TRANSACTION ID-{}",
                        cardNum, dailyTran.getDalytranId());
                continue;
            }

            CardXrefRecord xref = xrefOpt.get();
            log.info("SUCCESSFUL READ OF XREF - Card: {}, Account: {}, Customer: {}",
                    xref.getXrefCardNum(), xref.getXrefAcctId(), xref.getXrefCustId());

            // Read account
            Optional<AccountRecord> acctOpt = accountRepository.findById(xref.getXrefAcctId());
            if (acctOpt.isEmpty()) {
                log.warn("ACCOUNT {} NOT FOUND", xref.getXrefAcctId());
                continue;
            }

            log.info("SUCCESSFUL READ OF ACCOUNT FILE for account {}", xref.getXrefAcctId());
        }

        log.info("END OF EXECUTION OF PROGRAM CBTRN01C. Records processed: {}", count);
        return count;
    }
}
