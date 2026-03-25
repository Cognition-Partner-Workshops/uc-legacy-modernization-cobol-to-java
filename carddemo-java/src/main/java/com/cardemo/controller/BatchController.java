package com.cardemo.controller;

import com.cardemo.batch.AccountDataProcessor;
import com.cardemo.batch.CustomerDataProcessor;
import com.cardemo.batch.DataExporter;
import com.cardemo.batch.InterestCalculator;
import com.cardemo.batch.TransactionProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Batch Job Controller - provides REST endpoints to trigger batch jobs
 * that were originally executed via JCL on the mainframe.
 * 
 * Replaces JCL job submissions (DEFGJCL directory):
 * - ACTRPT -> /api/batch/account-report
 * - INTCALC -> /api/batch/interest-calculation
 * - CUSTRPT -> /api/batch/customer-report
 * - TRNREAD -> /api/batch/transaction-read
 * - TRNPOST -> /api/batch/transaction-posting
 * - EXPORT -> /api/batch/export
 */
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final AccountDataProcessor accountDataProcessor;
    private final CustomerDataProcessor customerDataProcessor;
    private final TransactionProcessor transactionProcessor;
    private final InterestCalculator interestCalculator;
    private final DataExporter dataExporter;

    public BatchController(AccountDataProcessor accountDataProcessor,
                           CustomerDataProcessor customerDataProcessor,
                           TransactionProcessor transactionProcessor,
                           InterestCalculator interestCalculator,
                           DataExporter dataExporter) {
        this.accountDataProcessor = accountDataProcessor;
        this.customerDataProcessor = customerDataProcessor;
        this.transactionProcessor = transactionProcessor;
        this.interestCalculator = interestCalculator;
        this.dataExporter = dataExporter;
    }

    /**
     * POST /api/batch/account-report
     * Run account/card/xref file reports (CBACT01C + CBACT02C + CBACT03C).
     */
    @PostMapping("/account-report")
    public ResponseEntity<Map<String, Object>> runAccountReport() {
        int accounts = accountDataProcessor.processAllAccounts();
        int cards = accountDataProcessor.processAllCards();
        int xrefs = accountDataProcessor.processAllCardXrefs();
        return ResponseEntity.ok(Map.of(
                "job", "accountReport",
                "accountsProcessed", accounts,
                "cardsProcessed", cards,
                "xrefsProcessed", xrefs,
                "status", "COMPLETED"
        ));
    }

    /**
     * POST /api/batch/interest-calculation
     * Run interest calculation (CBACT04C).
     */
    @PostMapping("/interest-calculation")
    public ResponseEntity<Map<String, Object>> runInterestCalculation() {
        InterestCalculator.InterestResult result = interestCalculator.calculateInterest();
        return ResponseEntity.ok(Map.of(
                "job", "interestCalculation",
                "accountsProcessed", result.accountsProcessed,
                "totalInterestApplied", result.totalInterestApplied.toString(),
                "status", "COMPLETED"
        ));
    }

    /**
     * POST /api/batch/customer-report
     * Run customer file report (CBCUS01C).
     */
    @PostMapping("/customer-report")
    public ResponseEntity<Map<String, Object>> runCustomerReport() {
        int count = customerDataProcessor.processAllCustomers();
        return ResponseEntity.ok(Map.of(
                "job", "customerReport",
                "customersProcessed", count,
                "status", "COMPLETED"
        ));
    }

    /**
     * POST /api/batch/transaction-read
     * Read all transactions (CBTRN01C).
     */
    @PostMapping("/transaction-read")
    public ResponseEntity<Map<String, Object>> runTransactionRead() {
        int count = transactionProcessor.readAllTransactions();
        return ResponseEntity.ok(Map.of(
                "job", "transactionRead",
                "transactionsRead", count,
                "status", "COMPLETED"
        ));
    }

    /**
     * POST /api/batch/transaction-posting
     * Post daily transactions (CBTRN02C).
     */
    @PostMapping("/transaction-posting")
    public ResponseEntity<Map<String, Object>> runTransactionPosting() {
        TransactionProcessor.PostingResult result = transactionProcessor.postDailyTransactions();
        return ResponseEntity.ok(Map.of(
                "job", "transactionPosting",
                "posted", result.posted,
                "rejected", result.rejected,
                "errors", result.errors,
                "status", "COMPLETED"
        ));
    }

    /**
     * POST /api/batch/export
     * Export all data (CBEXPORT).
     */
    @PostMapping("/export")
    public ResponseEntity<DataExporter.ExportResult> runExport() {
        return ResponseEntity.ok(dataExporter.exportAll());
    }

    /**
     * GET /api/batch/status
     * Check batch job capabilities (replaces JCL job status check).
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "availableJobs", java.util.List.of(
                        "account-report",
                        "interest-calculation",
                        "customer-report",
                        "transaction-read",
                        "transaction-posting",
                        "export"
                ),
                "status", "READY"
        ));
    }
}
