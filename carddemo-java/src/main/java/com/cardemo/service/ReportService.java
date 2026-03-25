package com.cardemo.service;

import com.cardemo.model.Transaction;
import com.cardemo.model.TransactionCategory;
import com.cardemo.model.TransactionCategoryKey;
import com.cardemo.model.TransactionType;
import com.cardemo.repository.TransactionCategoryRepository;
import com.cardemo.repository.TransactionRepository;
import com.cardemo.repository.TransactionTypeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Report Service - converted from COBOL programs CORPT00C.cbl and CBTRN03C.cbl
 * Original: CICS Transaction Reports screen (CORPT00C) and
 * Batch Transaction Report generator (CBTRN03C)
 */
@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    public ReportService(TransactionRepository transactionRepository,
                         TransactionTypeRepository transactionTypeRepository,
                         TransactionCategoryRepository transactionCategoryRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
    }

    /**
     * Generate a daily transaction report - equivalent to CBTRN03C batch program.
     * Groups transactions by account and calculates totals.
     */
    public TransactionReport generateDailyReport() {
        List<Transaction> allTransactions = transactionRepository.findAll();

        Map<String, List<TransactionReportLine>> accountGroups = new LinkedHashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Transaction tran : allTransactions) {
            String cardNum = tran.getCardNum() != null ? tran.getCardNum() : "UNKNOWN";

            TransactionReportLine line = new TransactionReportLine();
            line.tranId = tran.getTranId();
            line.cardNum = cardNum;
            line.typeCd = tran.getTypeCd();
            line.typeDesc = getTypeDescription(tran.getTypeCd());
            line.catCd = tran.getCatCd();
            line.catDesc = getCategoryDescription(tran.getTypeCd(), tran.getCatCd());
            line.source = tran.getSource();
            line.amount = tran.getAmount() != null ? tran.getAmount() : BigDecimal.ZERO;

            accountGroups.computeIfAbsent(cardNum, k -> new ArrayList<>()).add(line);
            grandTotal = grandTotal.add(line.amount);
        }

        TransactionReport report = new TransactionReport();
        report.reportName = "Daily Transaction Report";
        report.accountGroups = accountGroups;
        report.grandTotal = grandTotal;
        report.totalTransactions = allTransactions.size();

        return report;
    }

    private String getTypeDescription(String typeCd) {
        if (typeCd == null) return "";
        Optional<TransactionType> type = transactionTypeRepository.findById(typeCd);
        return type.map(TransactionType::getDescription).orElse("");
    }

    private String getCategoryDescription(String typeCd, Integer catCd) {
        if (typeCd == null || catCd == null) return "";
        Optional<TransactionCategory> cat = transactionCategoryRepository.findById(
                new TransactionCategoryKey(typeCd, catCd));
        return cat.map(TransactionCategory::getDescription).orElse("");
    }

    /**
     * Report data structure - equivalent to CVTRA07Y.cpy report layout
     */
    public static class TransactionReport {
        public String reportName;
        public Map<String, List<TransactionReportLine>> accountGroups;
        public BigDecimal grandTotal;
        public int totalTransactions;
    }

    public static class TransactionReportLine {
        public String tranId;
        public String cardNum;
        public String typeCd;
        public String typeDesc;
        public Integer catCd;
        public String catDesc;
        public String source;
        public BigDecimal amount;
    }
}
