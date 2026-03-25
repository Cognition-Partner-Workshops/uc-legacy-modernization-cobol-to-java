package com.carddemo.service;

import com.carddemo.dto.ReportRequest;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.BusinessValidationException;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Report service - replaces CORPT00C.
 * Original COBOL: constructs JCL records and submits to INTRDR (internal reader) via TDQ.
 * Java: generates reports directly or triggers async batch jobs.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Submit a report request - replaces CORPT00C's JCL job submission.
     * Returns report metadata including the report ID for status tracking.
     */
    public Map<String, Object> submitReport(ReportRequest request) {
        LocalDateTime startDate;
        LocalDateTime endDate;

        switch (request.getReportType()) {
            case "MONTHLY" -> {
                LocalDate now = LocalDate.now();
                startDate = now.withDayOfMonth(1).atStartOfDay();
                endDate = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
            }
            case "YEARLY" -> {
                LocalDate now = LocalDate.now();
                startDate = now.withDayOfYear(1).atStartOfDay();
                endDate = now.withDayOfYear(now.lengthOfYear()).atTime(LocalTime.MAX);
            }
            case "CUSTOM" -> {
                if (request.getStartDate() == null || request.getEndDate() == null) {
                    throw new BusinessValidationException("Start date and end date are required for CUSTOM reports");
                }
                startDate = DateUtils.parseDate(request.getStartDate()).atStartOfDay();
                endDate = DateUtils.parseDate(request.getEndDate()).atTime(LocalTime.MAX);
            }
            default -> throw new BusinessValidationException("Invalid report type: " + request.getReportType());
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessValidationException("Start date must be before end date");
        }

        String reportId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Report submitted: id={}, type={}, range={} to {}",
                reportId, request.getReportType(), startDate, endDate);

        Page<Transaction> transactions = transactionRepository
                .findByOriginatedTsBetweenOrderByOriginatedTsDesc(startDate, endDate, Pageable.unpaged());

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", reportId);
        result.put("reportType", request.getReportType());
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        result.put("totalTransactions", transactions.getTotalElements());
        result.put("status", "COMPLETED");

        return result;
    }
}
