package com.carddemo.batch;

import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Transaction Validation - from CBTRN01C.cbl + COMBTRAN.jcl
 * Read daily_transactions, validate card exists in card_xrefs,
 * write valid to transactions, invalid to rejected
 */
@Component
public class TransactionValidationTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(TransactionValidationTasklet.class);

    private final DailyTransactionRepository dailyTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;

    public TransactionValidationTasklet(DailyTransactionRepository dailyTransactionRepository,
                                        TransactionRepository transactionRepository,
                                        CardXrefRepository cardXrefRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<DailyTransaction> dailyTransactions = dailyTransactionRepository.findAll();
        int validCount = 0;
        int rejectCount = 0;

        for (DailyTransaction daily : dailyTransactions) {
            boolean cardExists = cardXrefRepository.existsById(daily.getCardNum());

            if (cardExists) {
                Transaction txn = new Transaction();
                txn.setCardNum(daily.getCardNum());
                txn.setTranId(daily.getTranId());
                txn.setTypeCd(daily.getTypeCd());
                txn.setCatCd(daily.getCatCd());
                txn.setSource(daily.getSource());
                txn.setDescription(daily.getDescription());
                txn.setAmount(daily.getAmount());
                txn.setMerchantId(daily.getMerchantId());
                txn.setMerchantName(daily.getMerchantName());
                txn.setMerchantCity(daily.getMerchantCity());
                txn.setMerchantZip(daily.getMerchantZip());
                txn.setOrigTimestamp(daily.getOrigTimestamp());
                txn.setProcTimestamp(daily.getProcTimestamp());
                transactionRepository.save(txn);
                validCount++;
            } else {
                log.warn("Rejected transaction - card not found in xref: {}", daily.getCardNum());
                rejectCount++;
            }
        }

        log.info("Transaction validation complete: {} valid, {} rejected", validCount, rejectCount);
        return RepeatStatus.FINISHED;
    }
}
