package com.carddemo.filematch.service;

import com.carddemo.filematch.model.ProcessingSummary;
import com.carddemo.filematch.model.Transaction;
import com.carddemo.filematch.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction file-match processing service.
 *
 * Java equivalent of COBOL program CBTRN02C:
 *   - Opens the daily transaction file (CSV instead of sequential file)
 *   - Reads each record
 *   - Validates and matches transactions by type code
 *   - Posts matched transactions to the database (instead of VSAM KSDS)
 *   - Writes rejected transactions to a reject list
 *   - Displays a processing summary
 */
@Service
public class TransactionFileMatchService {

    private static final Logger log = LoggerFactory.getLogger(TransactionFileMatchService.class);

    private final CsvService csvService;
    private final TransactionRepository transactionRepository;

    @Value("${app.match-criteria.transaction-type-code:SA}")
    private String matchTypeCd;

    public TransactionFileMatchService(CsvService csvService,
                                        TransactionRepository transactionRepository) {
        this.csvService = csvService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Process transactions from a CSV input stream.
     *
     * Equivalent to COBOL CBTRN02C PROCEDURE DIVISION:
     *   PERFORM 0000-DALYTRAN-OPEN
     *   PERFORM UNTIL END-OF-FILE = 'Y'
     *       PERFORM 1000-DALYTRAN-GET-NEXT
     *       PERFORM 1500-VALIDATE-TRAN
     *       IF WS-VALIDATION-FAIL-REASON = 0
     *           PERFORM 2000-POST-TRANSACTION
     *       ELSE
     *           PERFORM 2500-WRITE-REJECT-REC
     *       END-IF
     *   END-PERFORM
     *   PERFORM 9000-DALYTRAN-CLOSE
     */
    public ProcessingSummary processTransactionFile(InputStream inputStream) throws IOException {
        log.info("START OF EXECUTION OF PROGRAM CBTRN02C (Java)");
        log.info("Match criteria: TRAN-TYPE-CD = '{}'", matchTypeCd);

        List<Transaction> allTransactions = csvService.readTransactionsCsv(inputStream);
        List<Transaction> matchedTransactions = new ArrayList<>();
        int rejectCount = 0;

        for (Transaction tran : allTransactions) {
            // 1500-VALIDATE-TRAN equivalent
            if (isValidTransaction(tran)) {
                // Match by transaction type code
                if (matchTypeCd.equals(tran.getTranTypeCd())) {
                    matchedTransactions.add(tran);
                    log.debug("POSTED: {}", tran);
                } else {
                    rejectCount++;
                    log.debug("REJECTED (type mismatch): {} (type='{}')",
                            tran.getTranId(), tran.getTranTypeCd());
                }
            } else {
                rejectCount++;
                log.debug("REJECTED (validation): {}", tran.getTranId());
            }
        }

        // 2000-POST-TRANSACTION equivalent: persist to database
        transactionRepository.saveAll(matchedTransactions);
        log.info("TRANSACTIONS PROCESSED: {}", allTransactions.size());
        log.info("TRANSACTIONS MATCHED  : {}", matchedTransactions.size());
        log.info("TRANSACTIONS REJECTED : {}", rejectCount);

        log.info("END OF EXECUTION OF PROGRAM CBTRN02C (Java)");

        return new ProcessingSummary(
                "CBTRN02C",
                allTransactions.size(),
                matchedTransactions.size(),
                rejectCount,
                "TRAN-TYPE-CD = '" + matchTypeCd + "'",
                "COMPLETED"
        );
    }

    /**
     * Process transactions from a classpath resource file.
     */
    public ProcessingSummary processTransactionFile(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            return processTransactionFile(is);
        }
    }

    /**
     * Validate a transaction record.
     * Equivalent to COBOL 1500-VALIDATE-TRAN paragraph.
     */
    private boolean isValidTransaction(Transaction tran) {
        if (tran.getTranId() == null || tran.getTranId().isBlank()) {
            return false;
        }
        if (tran.getTranCardNum() == null || tran.getTranCardNum().isBlank()) {
            return false;
        }
        if (tran.getTranAmt() == null) {
            return false;
        }
        return true;
    }

    /**
     * Query matched transactions from the database.
     */
    public List<Transaction> getMatchedTransactions() {
        return transactionRepository.findByTranTypeCd(matchTypeCd);
    }

    /**
     * Query all persisted transactions.
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
