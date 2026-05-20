package com.carddemo.transaction.service;

import com.carddemo.common.dto.PageResponse;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.dto.AddTransactionRequest;
import com.carddemo.transaction.dto.TransactionDto;
import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public PageResponse<TransactionDto> listTransactions(String cardNum, Pageable pageable) {
        Page<Transaction> page = transactionRepository.findByCardNum(cardNum, pageable);
        List<TransactionDto> content = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public TransactionDto getTransaction(String tranId) {
        Transaction txn = transactionRepository.findByTranId(tranId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "tranId", tranId));
        return toDto(txn);
    }

    public TransactionDto addTransaction(AddTransactionRequest request) {
        String tranId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String procTs = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS"));

        Transaction txn = Transaction.builder()
                .tranId(tranId)
                .typeCd(request.getTypeCd())
                .catCd(request.getCatCd())
                .source(request.getSource())
                .description(request.getDescription())
                .amount(request.getAmount())
                .merchantId(request.getMerchantId())
                .merchantName(request.getMerchantName())
                .merchantCity(request.getMerchantCity())
                .merchantZip(request.getMerchantZip())
                .cardNum(request.getCardNum())
                .origTs(procTs)
                .procTs(procTs)
                .build();

        txn = transactionRepository.save(txn);
        return toDto(txn);
    }

    private TransactionDto toDto(Transaction t) {
        return TransactionDto.builder()
                .tranId(t.getTranId())
                .typeCd(t.getTypeCd())
                .catCd(t.getCatCd())
                .source(t.getSource())
                .description(t.getDescription())
                .amount(t.getAmount())
                .merchantId(t.getMerchantId())
                .merchantName(t.getMerchantName())
                .merchantCity(t.getMerchantCity())
                .merchantZip(t.getMerchantZip())
                .cardNum(t.getCardNum())
                .origTs(t.getOrigTs())
                .procTs(t.getProcTs())
                .build();
    }
}
