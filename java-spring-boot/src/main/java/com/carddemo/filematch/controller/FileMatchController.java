package com.carddemo.filematch.controller;

import com.carddemo.filematch.model.Account;
import com.carddemo.filematch.model.ProcessingSummary;
import com.carddemo.filematch.model.Transaction;
import com.carddemo.filematch.service.AccountFileMatchService;
import com.carddemo.filematch.service.CsvService;
import com.carddemo.filematch.service.TransactionFileMatchService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST controller exposing endpoints equivalent to JCL batch job submission.
 *
 * Replaces the mainframe workflow of:
 *   1. Submitting JCL (e.g., ACCTFILE, POSTTRAN jobs)
 *   2. Checking job output via JES spool
 *   3. Viewing results in VSAM files
 *
 * With REST endpoints:
 *   POST /api/process/accounts      - Upload and process account CSV
 *   POST /api/process/transactions  - Upload and process transaction CSV
 *   POST /api/process/sample        - Process bundled sample data
 *   GET  /api/accounts              - Query matched accounts
 *   GET  /api/transactions          - Query matched transactions
 *   GET  /api/export/accounts       - Download matched accounts as CSV
 *   GET  /api/export/transactions   - Download matched transactions as CSV
 */
@RestController
@RequestMapping("/api")
public class FileMatchController {

    private static final Logger log = LoggerFactory.getLogger(FileMatchController.class);

    private final AccountFileMatchService accountService;
    private final TransactionFileMatchService transactionService;
    private final CsvService csvService;

    public FileMatchController(AccountFileMatchService accountService,
                                TransactionFileMatchService transactionService,
                                CsvService csvService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.csvService = csvService;
    }

    // ── Process Endpoints (replaces JCL job submission) ─────────────────

    /**
     * Upload and process an account CSV file.
     * Equivalent to submitting the ACCTFILE JCL job.
     */
    @PostMapping("/process/accounts")
    public ResponseEntity<ProcessingSummary> processAccounts(
            @RequestParam("file") MultipartFile file) {
        try {
            ProcessingSummary summary = accountService.processAccountFile(file.getInputStream());
            return ResponseEntity.ok(summary);
        } catch (IOException e) {
            log.error("Error processing account file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload and process a transaction CSV file.
     * Equivalent to submitting the POSTTRAN JCL job.
     */
    @PostMapping("/process/transactions")
    public ResponseEntity<ProcessingSummary> processTransactions(
            @RequestParam("file") MultipartFile file) {
        try {
            ProcessingSummary summary =
                    transactionService.processTransactionFile(file.getInputStream());
            return ResponseEntity.ok(summary);
        } catch (IOException e) {
            log.error("Error processing transaction file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Process the bundled sample data files.
     * Convenience endpoint for testing — loads accounts.csv and transactions.csv
     * from the classpath.
     */
    @PostMapping("/process/sample")
    public ResponseEntity<Map<String, ProcessingSummary>> processSampleData() {
        try {
            ProcessingSummary acctSummary = accountService.processAccountFile(
                    new ClassPathResource("data/accounts.csv"));
            ProcessingSummary tranSummary = transactionService.processTransactionFile(
                    new ClassPathResource("data/transactions.csv"));

            return ResponseEntity.ok(Map.of(
                    "accounts", acctSummary,
                    "transactions", tranSummary
            ));
        } catch (IOException e) {
            log.error("Error processing sample data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── Query Endpoints (replaces VSAM browse / CICS inquiry) ───────────

    /** Get all matched (active) accounts from the database. */
    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAccounts() {
        return ResponseEntity.ok(accountService.getMatchedAccounts());
    }

    /** Get all matched transactions from the database. */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions() {
        return ResponseEntity.ok(transactionService.getMatchedTransactions());
    }

    // ── Export Endpoints (CSV download) ─────────────────────────────────

    /**
     * Export matched accounts as a CSV file download.
     * Equivalent to COBOL: WRITE OUT-ACCT-REC to sequential output file.
     */
    @GetMapping("/export/accounts")
    public void exportAccounts(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"matched_accounts.csv\"");
        List<Account> accounts = accountService.getMatchedAccounts();
        csvService.writeAccountsCsv(accounts, response.getOutputStream());
    }

    /**
     * Export matched transactions as a CSV file download.
     * Equivalent to COBOL: WRITE FD-TRANFILE-REC to transaction master.
     */
    @GetMapping("/export/transactions")
    public void exportTransactions(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"matched_transactions.csv\"");
        List<Transaction> transactions = transactionService.getMatchedTransactions();
        csvService.writeTransactionsCsv(transactions, response.getOutputStream());
    }
}
