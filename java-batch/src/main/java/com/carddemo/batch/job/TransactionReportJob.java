package com.carddemo.batch.job;

import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.TransactionCategoryRecord;
import com.carddemo.batch.model.TransactionRecord;
import com.carddemo.batch.model.TransactionTypeRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java equivalent of COBOL program CBTRN03C.CBL - Transaction Detail Report.
 *
 * Reads the posted transaction file sequentially, filters by a date range
 * (from a date parameter file), looks up transaction type and category
 * descriptions, and produces a formatted report with page totals, account
 * totals, and a grand total.
 *
 * Report layout (from COBOL copybook CVTRA07Y):
 *   - Report name header with date range
 *   - Transaction detail lines with: Tran ID, Account ID, Type, Category, Source, Amount
 *   - Page totals every PAGE_SIZE (20) lines
 *   - Account totals when card number changes
 *   - Grand total at end
 */
public class TransactionReportJob {

    private static final int PAGE_SIZE = 20;

    private final List<TransactionRecord> transactions;
    private final Map<String, CardXrefRecord> xrefByCardNum;
    private final Map<String, TransactionTypeRecord> tranTypeByCode;
    private final Map<String, TransactionCategoryRecord> tranCatByKey;

    private final List<String> reportLines = new ArrayList<>();
    private int lineCounter = 0;
    private BigDecimal pageTotal = BigDecimal.ZERO;
    private BigDecimal accountTotal = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private String currentCardNum = "";
    private boolean firstTime = true;

    public TransactionReportJob(
            List<TransactionRecord> transactions,
            Map<String, CardXrefRecord> xrefByCardNum,
            Map<String, TransactionTypeRecord> tranTypeByCode,
            Map<String, TransactionCategoryRecord> tranCatByKey) {
        this.transactions = transactions;
        this.xrefByCardNum = xrefByCardNum;
        this.tranTypeByCode = tranTypeByCode;
        this.tranCatByKey = tranCatByKey;
    }

    /**
     * Execute the transaction report batch job.
     * Equivalent to COBOL PROCEDURE DIVISION main logic.
     */
    public int execute(String startDate, String endDate) {
        System.out.println("START OF EXECUTION OF PROGRAM CBTRN03C (Java)");
        System.out.println("Reporting from " + startDate + " to " + endDate);

        for (TransactionRecord tran : transactions) {
            // Filter by date range (equivalent to COBOL date range check)
            String tranDate = tran.getProcTimestamp();
            if (tranDate != null && tranDate.length() >= 10) {
                tranDate = tranDate.substring(0, 10);
                if (tranDate.compareTo(startDate) < 0 || tranDate.compareTo(endDate) > 0) {
                    continue;
                }
            }

            System.out.println(tran);

            // Check if card number changed - write account totals
            if (!currentCardNum.equals(tran.getCardNum())) {
                if (!firstTime) {
                    writeAccountTotals();
                }
                currentCardNum = tran.getCardNum();
            }

            // Write report detail
            writeTransactionReport(tran, startDate, endDate);
        }

        // Write final totals
        if (!firstTime) {
            pageTotal = pageTotal.add(BigDecimal.ZERO); // ensure initialized
            writePageTotals();
            writeGrandTotals();
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBTRN03C (Java)");
        return 0;
    }

    /**
     * Write transaction report detail line.
     * Equivalent to COBOL 1100-WRITE-TRANSACTION-REPORT + 1120-WRITE-DETAIL.
     */
    private void writeTransactionReport(TransactionRecord tran, String startDate, String endDate) {
        if (firstTime) {
            firstTime = false;
            writeHeaders(startDate, endDate);
        }

        // Check page break
        if (lineCounter > 0 && lineCounter % PAGE_SIZE == 0) {
            writePageTotals();
            writeHeaders(startDate, endDate);
        }

        pageTotal = pageTotal.add(tran.getAmount());
        accountTotal = accountTotal.add(tran.getAmount());
        writeDetail(tran);
    }

    /**
     * Write report headers.
     * Equivalent to COBOL 1120-WRITE-HEADERS.
     */
    private void writeHeaders(String startDate, String endDate) {
        // REPORT-NAME-HEADER
        reportLines.add(String.format("%-38s%-41sDate Range: %s to %s",
                "DALYREPT", "Daily Transaction Report", startDate, endDate));
        lineCounter++;

        reportLines.add(""); // blank line
        lineCounter++;

        // TRANSACTION-HEADER-1
        reportLines.add(String.format("%-17s%-12s%-19s%-35s%-14s %16s",
                "Transaction ID", "Account ID", "Transaction Type",
                "Tran Category", "Tran Source", "Amount"));
        lineCounter++;

        // TRANSACTION-HEADER-2
        reportLines.add("-".repeat(133));
        lineCounter++;
    }

    /**
     * Write transaction detail line.
     * Equivalent to COBOL 1120-WRITE-DETAIL.
     */
    private void writeDetail(TransactionRecord tran) {
        CardXrefRecord xref = xrefByCardNum.get(tran.getCardNum());
        long acctId = xref != null ? xref.getAcctId() : 0;

        // Look up transaction type description
        TransactionTypeRecord tranType = tranTypeByCode.get(tran.getTypeCd());
        String typeDesc = tranType != null ? tranType.getDescription() : "";
        if (typeDesc.length() > 15) typeDesc = typeDesc.substring(0, 15);

        // Look up transaction category description
        String catKey = String.format("%2s%04d", tran.getTypeCd(), tran.getCatCd());
        TransactionCategoryRecord tranCat = tranCatByKey.get(catKey);
        String catDesc = tranCat != null ? tranCat.getDescription() : "";
        if (catDesc.length() > 29) catDesc = catDesc.substring(0, 29);

        String source = tran.getSource() != null ? tran.getSource().trim() : "";
        if (source.length() > 10) source = source.substring(0, 10);

        reportLines.add(String.format("%-16s %-11s %-2s-%-15s %-4d-%-29s %-10s    %,15.2f",
                tran.getTranId(),
                String.format("%011d", acctId),
                tran.getTypeCd(),
                typeDesc,
                tran.getCatCd(),
                catDesc,
                source,
                tran.getAmount()));
        lineCounter++;
    }

    /**
     * Write page totals.
     * Equivalent to COBOL 1110-WRITE-PAGE-TOTALS.
     */
    private void writePageTotals() {
        reportLines.add(String.format("Page Total%s%+,15.2f",
                ".".repeat(86), pageTotal));
        grandTotal = grandTotal.add(pageTotal);
        pageTotal = BigDecimal.ZERO;
        lineCounter++;

        reportLines.add("-".repeat(133));
        lineCounter++;
    }

    /**
     * Write account totals.
     * Equivalent to COBOL 1120-WRITE-ACCOUNT-TOTALS.
     */
    private void writeAccountTotals() {
        reportLines.add(String.format("Account Total%s%+,15.2f",
                ".".repeat(84), accountTotal));
        accountTotal = BigDecimal.ZERO;
        lineCounter++;

        reportLines.add("-".repeat(133));
        lineCounter++;
    }

    /**
     * Write grand totals.
     * Equivalent to COBOL 1110-WRITE-GRAND-TOTALS.
     */
    private void writeGrandTotals() {
        reportLines.add(String.format("Grand Total%s%+,15.2f",
                ".".repeat(86), grandTotal));
    }

    public List<String> getReportLines() { return reportLines; }
}
