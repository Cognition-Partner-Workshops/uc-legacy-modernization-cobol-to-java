package com.carddemo.transaction.service;

import com.carddemo.transaction.dto.ReportRequest;
import com.carddemo.transaction.dto.TransactionDto;
import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionDto> generateTransactionReport(ReportRequest request) {
        List<Transaction> transactions = transactionRepository.findAll();

        return transactions.stream()
                .filter(t -> filterByDateRange(t, request.getStartDate(), request.getEndDate()))
                .filter(t -> request.getAcctId() == null || true)
                .map(t -> TransactionDto.builder()
                        .tranId(t.getTranId())
                        .typeCd(t.getTypeCd())
                        .catCd(t.getCatCd())
                        .source(t.getSource())
                        .description(t.getDescription())
                        .amount(t.getAmount())
                        .cardNum(t.getCardNum())
                        .origTs(t.getOrigTs())
                        .procTs(t.getProcTs())
                        .build())
                .collect(Collectors.toList());
    }

    private boolean filterByDateRange(Transaction t, String startDate, String endDate) {
        if (startDate == null && endDate == null) return true;
        String origDate = t.getOrigTs() != null && t.getOrigTs().length() >= 10
                ? t.getOrigTs().substring(0, 10) : "";
        if (startDate != null && origDate.compareTo(startDate) < 0) return false;
        if (endDate != null && origDate.compareTo(endDate) > 0) return false;
        return true;
    }
}
