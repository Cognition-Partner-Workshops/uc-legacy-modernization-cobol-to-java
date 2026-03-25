/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.batch;

import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.TransactionCategoryRecord;
import com.cardemo.model.TransactionRecord;
import com.cardemo.model.TransactionTypeRecord;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionCategoryRepository;
import com.cardemo.repository.TransactionRepository;
import com.cardemo.repository.TransactionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Report Service - migrated from COBOL batch program CBTRN03C.cbl.
 * Prints transaction detail report with date range filtering,
 * page/account/grand totals.
 * Original: Reads TRANSACT, CARDXREF, TRANTYPE, TRANCATG files;
 *           writes REPORT-FILE with headers, detail lines, and totals.
 */
@Service
public class TransactionReportService {

    private static final Logger log = LoggerFactory.getLogger(TransactionReportService.class);

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    public TransactionReportService(TransactionRepository transactionRepository,
                                    CardXrefRepository cardXrefRepository,
                                    TransactionTypeRepository transactionTypeRepository,
                                    TransactionCategoryRepository transactionCategoryRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
    }

    /**
     * Generate transaction detail report - migrated from main PROCEDURE DIVISION.
     *
     * @param startDate report start date (YYYY-MM-DD)
     * @param endDate   report end date (YYYY-MM-DD)
     * @return list of report lines
     */
    public ReportResult generateReport(String startDate, String endDate) {
        log.info("START OF EXECUTION OF PROGRAM CBTRN03C (TransactionReport)");
        log.info("Reporting from {} to {}", startDate, endDate);

        List<TransactionRecord> transactions = transactionRepository.findAll();
        List<String> reportLines = new ArrayList<>();
        BigDecimal pageTotal = BigDecimal.ZERO;
        BigDecimal accountTotal = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;
        String currentCardNum = "";
        int lineCounter = 0;
        int pageSize = 20;

        // Write report header
        reportLines.add(String.format("TRANSACTION DETAIL REPORT  From: %s  To: %s", startDate, endDate));
        reportLines.add("");
        reportLines.add(String.format("%-16s %-11s %-2s %-20s %-4s %-20s %-10s %12s",
                "TRAN-ID", "ACCOUNT-ID", "TC", "TYPE-DESC", "CC", "CAT-DESC", "SOURCE", "AMOUNT"));
        reportLines.add(String.format("%s", "-".repeat(110)));

        for (TransactionRecord tran : transactions) {
            // Filter by date range
            String procTs = tran.getTranProcTs();
            if (procTs != null && procTs.length() >= 10) {
                String tranDate = procTs.substring(0, 10);
                if (startDate != null && tranDate.compareTo(startDate) < 0) continue;
                if (endDate != null && tranDate.compareTo(endDate) > 0) continue;
            }

            // Account change - print account totals
            String tranCardNum = tran.getTranCardNum() != null ? tran.getTranCardNum() : "";
            if (!currentCardNum.equals(tranCardNum)) {
                if (!currentCardNum.isEmpty()) {
                    reportLines.add(String.format("  Account Total: %12.2f", accountTotal));
                    reportLines.add("");
                    grandTotal = grandTotal.add(accountTotal);
                    accountTotal = BigDecimal.ZERO;
                }
                currentCardNum = tranCardNum;

                // Look up xref for account ID
                Optional<CardXrefRecord> xrefOpt = currentCardNum.isEmpty()
                        ? Optional.empty()
                        : cardXrefRepository.findById(currentCardNum);
                if (xrefOpt.isPresent()) {
                    reportLines.add(String.format("Card: %s  Account: %d  Customer: %d",
                            currentCardNum, xrefOpt.get().getXrefAcctId(), xrefOpt.get().getXrefCustId()));
                }
            }

            // Look up type and category descriptions
            String typeDesc = "";
            Optional<TransactionTypeRecord> typeOpt = transactionTypeRepository.findById(tran.getTranTypeCd());
            if (typeOpt.isPresent()) {
                typeDesc = typeOpt.get().getTranTypeDesc();
            }

            String catDesc = "";

            // Write detail line
            BigDecimal amt = tran.getTranAmt() != null ? tran.getTranAmt() : BigDecimal.ZERO;
            reportLines.add(String.format("%-16s %-11s %-2s %-20s %4d %-20s %-10s %12.2f",
                    tran.getTranId(),
                    "",
                    tran.getTranTypeCd(),
                    typeDesc,
                    tran.getTranCatCd(),
                    catDesc,
                    tran.getTranSource(),
                    amt));

            pageTotal = pageTotal.add(amt);
            accountTotal = accountTotal.add(amt);
            lineCounter++;

            // Page break
            if (lineCounter % pageSize == 0) {
                reportLines.add(String.format("  Page Total: %12.2f", pageTotal));
                pageTotal = BigDecimal.ZERO;
                reportLines.add("");
                reportLines.add(String.format("%s", "-".repeat(110)));
            }
        }

        // Final totals
        if (!currentCardNum.isEmpty()) {
            reportLines.add(String.format("  Account Total: %12.2f", accountTotal));
            grandTotal = grandTotal.add(accountTotal);
        }
        reportLines.add("");
        reportLines.add(String.format("  GRAND TOTAL: %12.2f", grandTotal));

        log.info("END OF EXECUTION OF PROGRAM CBTRN03C");

        return new ReportResult(reportLines, grandTotal);
    }

    public static class ReportResult {
        private final List<String> reportLines;
        private final BigDecimal grandTotal;

        public ReportResult(List<String> reportLines, BigDecimal grandTotal) {
            this.reportLines = reportLines;
            this.grandTotal = grandTotal;
        }

        public List<String> getReportLines() { return reportLines; }
        public BigDecimal getGrandTotal() { return grandTotal; }
    }
}
