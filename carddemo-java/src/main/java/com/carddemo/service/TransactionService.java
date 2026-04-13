package com.carddemo.service;

import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionId;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;

    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    public TransactionService(TransactionRepository transactionRepository,
                              CardXrefRepository cardXrefRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * List transactions for a card - mirrors COTRN00C.cbl
     */
    public Page<Transaction> listTransactions(String cardNum, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByCardNum(cardNum, pageable);
    }

    public List<Transaction> listTransactions(String cardNum) {
        return transactionRepository.findByCardNum(cardNum);
    }

    /**
     * View transaction detail - mirrors COTRN01C.cbl
     */
    public Transaction viewTransaction(String cardNum, String tranId) {
        TransactionId id = new TransactionId(cardNum, tranId);
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaction not found: " + cardNum + "/" + tranId));
    }

    /**
     * Add transaction - mirrors COTRN02C.cbl
     * Validates card exists via XREF before creating transaction
     */
    @Transactional
    public Transaction addTransaction(Transaction transaction) {
        // Validate card exists in cross-reference (COTRN02C validation)
        CardXref xref = cardXrefRepository.findById(transaction.getCardNum())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Card not found in cross-reference: " + transaction.getCardNum()));

        // Generate transaction ID if not provided
        if (transaction.getTranId() == null || transaction.getTranId().trim().isEmpty()) {
            String tranId = String.format("%016d", System.currentTimeMillis() % 10000000000000000L);
            transaction.setTranId(tranId);
        }

        // Set processing timestamp
        if (transaction.getProcTimestamp() == null) {
            transaction.setProcTimestamp(LocalDateTime.now().format(TS_FORMATTER));
        }

        return transactionRepository.save(transaction);
    }

    /**
     * Find transactions by date range
     */
    public List<Transaction> findByDateRange(String cardNum, String startDate, String endDate) {
        return transactionRepository.findByCardNumAndDateRange(cardNum, startDate, endDate);
    }
}
