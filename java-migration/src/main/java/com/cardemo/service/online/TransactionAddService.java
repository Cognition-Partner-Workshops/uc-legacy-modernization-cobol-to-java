/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.online;

import com.cardemo.common.DateTimeUtil;
import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.TransactionRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Add Service - migrated from COBOL program COTRN02C.cbl.
 * Handles adding new transactions.
 * Original: CICS program with TRANID CT02, writes to TRANSACT file.
 */
@Service
public class TransactionAddService {

    private static final Logger log = LoggerFactory.getLogger(TransactionAddService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;

    public TransactionAddService(TransactionRepository transactionRepository,
                                 AccountRepository accountRepository,
                                 CardXrefRepository cardXrefRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * Add a new transaction.
     * Migrated from PROCESS-ENTER-KEY and WRITE-TRANSACT-FILE paragraphs.
     */
    @Transactional
    public TransactionRecord addTransaction(String cardNum, String tranTypeCd,
                                            int tranCatCd, String tranSource,
                                            String tranDesc, BigDecimal tranAmt,
                                            long merchantId, String merchantName,
                                            String merchantCity, String merchantZip) {
        // Validate card exists in xref
        Optional<CardXrefRecord> xrefOpt = cardXrefRepository.findById(cardNum);
        if (xrefOpt.isEmpty()) {
            throw new TransactionAddException("Card number not found in cross-reference...");
        }

        // Validate account exists
        CardXrefRecord xref = xrefOpt.get();
        Optional<AccountRecord> acctOpt = accountRepository.findById(xref.getXrefAcctId());
        if (acctOpt.isEmpty()) {
            throw new TransactionAddException("Associated account not found...");
        }

        // Generate next transaction ID
        String nextTranId = generateNextTranId();

        // Build transaction record
        TransactionRecord tran = new TransactionRecord();
        tran.setTranId(nextTranId);
        tran.setTranTypeCd(tranTypeCd);
        tran.setTranCatCd(tranCatCd);
        tran.setTranSource(tranSource);
        tran.setTranDesc(tranDesc);
        tran.setTranAmt(tranAmt);
        tran.setTranMerchantId(merchantId);
        tran.setTranMerchantName(merchantName);
        tran.setTranMerchantCity(merchantCity);
        tran.setTranMerchantZip(merchantZip);
        tran.setTranCardNum(cardNum);

        String timestamp = DateTimeUtil.getCurrentTimestamp();
        tran.setTranOrigTs(timestamp);
        tran.setTranProcTs(timestamp);

        TransactionRecord saved = transactionRepository.save(tran);
        log.info("Transaction {} added successfully for card {}", nextTranId, cardNum);
        return saved;
    }

    /**
     * Generate next transaction ID - migrated from READPREV + ADD 1 logic.
     */
    private String generateNextTranId() {
        List<TransactionRecord> lastTrans = transactionRepository
                .findByTranIdLessThanEqualOrderByTranIdDesc("9999999999999999",
                        PageRequest.of(0, 1))
                .getContent();

        long nextId = 1;
        if (!lastTrans.isEmpty()) {
            try {
                nextId = Long.parseLong(lastTrans.get(0).getTranId().trim()) + 1;
            } catch (NumberFormatException e) {
                nextId = 1;
            }
        }
        return String.format("%016d", nextId);
    }

    public static class TransactionAddException extends RuntimeException {
        public TransactionAddException(String message) {
            super(message);
        }
    }
}
