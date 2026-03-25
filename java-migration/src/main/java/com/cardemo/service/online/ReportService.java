package com.cardemo.service.online;

import com.cardemo.model.TransactionRecord;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Report Service - migrated from COBOL program CORPT00C.cbl.
 * Handles generating transaction reports.
 * Original: CICS program with TRANID CR00, submits batch job for report generation.
 * In Java migration, report is generated on-demand.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generate a transaction summary report.
     * Migrated from batch report generation (COBOL CORPT00C initiating batch job).
     */
    public Map<String, Object> generateTransactionReport(String startDate, String endDate) {
        List<TransactionRecord> allTransactions = transactionRepository
                .findAll(Sort.by("tranOrigTs"));

        // Filter by date range if provided
        List<TransactionRecord> filtered = allTransactions.stream()
                .filter(t -> {
                    String ts = t.getTranOrigTs();
                    if (ts == null) return false;
                    if (startDate != null && !startDate.isEmpty() && ts.compareTo(startDate) < 0) return false;
                    if (endDate != null && !endDate.isEmpty() && ts.compareTo(endDate) > 0) return false;
                    return true;
                })
                .collect(Collectors.toList());

        // Calculate totals
        BigDecimal grandTotal = filtered.stream()
                .map(TransactionRecord::getTranAmt)
                .filter(amt -> amt != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by type
        Map<String, BigDecimal> totalsByType = filtered.stream()
                .filter(t -> t.getTranTypeCd() != null && t.getTranAmt() != null)
                .collect(Collectors.groupingBy(
                        TransactionRecord::getTranTypeCd,
                        Collectors.reducing(BigDecimal.ZERO,
                                TransactionRecord::getTranAmt,
                                BigDecimal::add)));

        Map<String, Object> report = new HashMap<>();
        report.put("transactions", filtered);
        report.put("transactionCount", filtered.size());
        report.put("grandTotal", grandTotal);
        report.put("totalsByType", totalsByType);
        report.put("startDate", startDate);
        report.put("endDate", endDate);

        log.info("Report generated: {} transactions, grand total: {}", filtered.size(), grandTotal);
        return report;
    }
}
