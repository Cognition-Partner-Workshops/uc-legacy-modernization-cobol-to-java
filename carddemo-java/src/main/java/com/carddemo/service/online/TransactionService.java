package com.carddemo.service.online;

import com.carddemo.dto.TransactionAddRequest;
import com.carddemo.dto.TransactionDto;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.exception.ValidationException;
import com.carddemo.model.CardXref;
import com.carddemo.model.DailyTransaction;
import com.carddemo.model.Transaction;
import com.carddemo.model.TransactionCategory;
import com.carddemo.model.TransactionType;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.repository.TransactionTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Transaction service replacing COBOL programs COTRN00C, COTRN01C, COTRN02C.
 *
 * <p>COTRN00C (Transaction List, CICS txn CT00):
 * - Browses TRANSACT file (STARTBR/READNEXT)
 * - Displays paginated transaction list
 *
 * <p>COTRN01C (Transaction Detail, CICS txn CT01):
 * - Reads TRANSACT by transaction ID
 *
 * <p>COTRN02C (Transaction Add, CICS txn CT02):
 * - Validates card via XREFFILE
 * - Writes new daily transaction record to DALYTRAN
 * - Pending batch processing by CBTRN02C
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final DailyTransactionRepository dailyTransactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              DailyTransactionRepository dailyTransactionRepository,
                              CardXrefRepository cardXrefRepository,
                              TransactionTypeRepository transactionTypeRepository,
                              TransactionCategoryRepository transactionCategoryRepository) {
        this.transactionRepository = transactionRepository;
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
    }

    /**
     * List transactions with pagination - replaces COTRN00C browse.
     */
    @Transactional(readOnly = true)
    public Page<TransactionDto> listTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::mapToDto);
    }

    /**
     * List transactions by card number - replaces COTRN00C filtered browse.
     */
    @Transactional(readOnly = true)
    public Page<TransactionDto> listTransactionsByCard(String cardNum, Pageable pageable) {
        return transactionRepository.findByTranCardNum(cardNum, pageable).map(this::mapToDto);
    }

    /**
     * Get transaction detail - replaces COTRN01C READ paragraph.
     */
    @Transactional(readOnly = true)
    public TransactionDto getTransaction(String tranId) {
        Transaction tran = transactionRepository.findById(tranId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", tranId));
        return mapToDto(tran);
    }

    /**
     * Add new transaction - replaces COTRN02C PROCESS-ENTER-KEY paragraph.
     * Creates a daily transaction for batch processing.
     */
    @Transactional
    public TransactionDto addTransaction(TransactionAddRequest request) {
        // Validate card exists via xref (replaces COTRN02C XREFFILE READ)
        Optional<CardXref> xref = cardXrefRepository.findById(request.getTranCardNum());
        if (xref.isEmpty()) {
            throw new ValidationException("Card number not found in cross-reference file");
        }

        // Validate transaction type
        Optional<TransactionType> tranType = transactionTypeRepository.findById(request.getTranTypeCd());
        if (tranType.isEmpty()) {
            throw new ValidationException("Invalid transaction type code: " + request.getTranTypeCd());
        }

        // Validate transaction category
        Optional<TransactionCategory> tranCat = transactionCategoryRepository.findById(request.getTranCatCd());
        if (tranCat.isEmpty()) {
            throw new ValidationException("Invalid transaction category code: " + request.getTranCatCd());
        }

        // Create daily transaction (pending batch processing)
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        String tranId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        DailyTransaction daily = new DailyTransaction();
        daily.setDalytranId(tranId);
        daily.setDalytranTypeCd(request.getTranTypeCd());
        daily.setDalytranCatCd(request.getTranCatCd());
        daily.setDalytranSource(request.getTranSource() != null ? request.getTranSource() : "ONLINE");
        daily.setDalytranDesc(request.getTranDesc());
        daily.setDalytranAmt(request.getTranAmt());
        daily.setDalytranMerchantId(request.getTranMerchantId());
        daily.setDalytranMerchantName(request.getTranMerchantName());
        daily.setDalytranMerchantCity(request.getTranMerchantCity());
        daily.setDalytranMerchantZip(request.getTranMerchantZip());
        daily.setDalytranCardNum(request.getTranCardNum());
        daily.setDalytranOrigTs(now);
        daily.setDalytranProcTs(null);
        daily.setProcessed(false);

        dailyTransactionRepository.save(daily);

        // Return as TransactionDto
        TransactionDto dto = new TransactionDto();
        dto.setTranId(tranId);
        dto.setTranTypeCd(request.getTranTypeCd());
        dto.setTranCatCd(request.getTranCatCd());
        dto.setTranSource(daily.getDalytranSource());
        dto.setTranDesc(request.getTranDesc());
        dto.setTranAmt(request.getTranAmt());
        dto.setTranMerchantId(request.getTranMerchantId());
        dto.setTranMerchantName(request.getTranMerchantName());
        dto.setTranMerchantCity(request.getTranMerchantCity());
        dto.setTranMerchantZip(request.getTranMerchantZip());
        dto.setTranCardNum(request.getTranCardNum());
        dto.setTranOrigTs(now);
        dto.setTranTypeDesc(tranType.get().getTranTypeDesc());
        dto.setTranCatDesc(tranCat.get().getTranCatDesc());
        return dto;
    }

    private TransactionDto mapToDto(Transaction tran) {
        TransactionDto dto = new TransactionDto();
        dto.setTranId(tran.getTranId());
        dto.setTranTypeCd(tran.getTranTypeCd());
        dto.setTranCatCd(tran.getTranCatCd());
        dto.setTranSource(tran.getTranSource());
        dto.setTranDesc(tran.getTranDesc());
        dto.setTranAmt(tran.getTranAmt());
        dto.setTranMerchantId(tran.getTranMerchantId());
        dto.setTranMerchantName(tran.getTranMerchantName());
        dto.setTranMerchantCity(tran.getTranMerchantCity());
        dto.setTranMerchantZip(tran.getTranMerchantZip());
        dto.setTranCardNum(tran.getTranCardNum());
        dto.setTranOrigTs(tran.getTranOrigTs());
        dto.setTranProcTs(tran.getTranProcTs());

        // Resolve type and category descriptions
        transactionTypeRepository.findById(tran.getTranTypeCd())
                .ifPresent(t -> dto.setTranTypeDesc(t.getTranTypeDesc()));
        transactionCategoryRepository.findById(tran.getTranCatCd())
                .ifPresent(c -> dto.setTranCatDesc(c.getTranCatDesc()));

        return dto;
    }
}
