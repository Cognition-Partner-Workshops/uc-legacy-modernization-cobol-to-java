package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.CustomerRecord;
import com.carddemo.batch.model.TransactionRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java equivalent of COBOL program CBSTM03A.CBL - Statement Generation.
 *
 * Generates account statements in both plain text and HTML formats
 * by iterating through card cross-references, looking up customer and
 * account data, and listing associated transactions.
 *
 * Notable COBOL patterns exercised and preserved in this migration:
 *   1. Mainframe control block addressing (PSA/TCB/TIOT) - replaced with logging
 *   2. ALTER and GO TO statements - replaced with method dispatch
 *   3. COMP and COMP-3 variables - replaced with int/BigDecimal
 *   4. 2-dimensional array (WS-TRNX-TABLE) - replaced with Map of Lists
 *   5. Call to subroutine (CBSTM03B) - inlined as method calls
 */
public class StatementGenerationJob {

    private final Map<String, CardXrefRecord> xrefByCardNum;
    private final List<CardXrefRecord> xrefRecords;
    private final Map<Long, CustomerRecord> customersByCustomerId;
    private final Map<Long, AccountRecord> accountsByAcctId;
    private final Map<String, List<TransactionRecord>> transactionsByCardNum;

    private final List<String> statementLines = new ArrayList<>();
    private final List<String> htmlLines = new ArrayList<>();

    public StatementGenerationJob(
            List<CardXrefRecord> xrefRecords,
            Map<String, CardXrefRecord> xrefByCardNum,
            Map<Long, CustomerRecord> customersByCustomerId,
            Map<Long, AccountRecord> accountsByAcctId,
            Map<String, List<TransactionRecord>> transactionsByCardNum) {
        this.xrefRecords = xrefRecords;
        this.xrefByCardNum = xrefByCardNum;
        this.customersByCustomerId = customersByCustomerId;
        this.accountsByAcctId = accountsByAcctId;
        this.transactionsByCardNum = transactionsByCardNum;
    }

    /**
     * Execute the statement generation batch job.
     * Equivalent to COBOL PROCEDURE DIVISION main logic through 1000-MAINLINE.
     */
    public int execute() {
        System.out.println("START OF EXECUTION OF PROGRAM CBSTM03A (Java)");

        for (CardXrefRecord xref : xrefRecords) {
            CustomerRecord customer = customersByCustomerId.get(xref.getCustId());
            AccountRecord account = accountsByAcctId.get(xref.getAcctId());

            if (customer == null || account == null) {
                System.out.println("Missing customer or account for card: " + xref.getCardNum());
                continue;
            }

            List<TransactionRecord> transactions =
                    transactionsByCardNum.getOrDefault(xref.getCardNum(), List.of());

            createStatement(xref, customer, account, transactions);
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBSTM03A (Java)");
        return 0;
    }

    /**
     * Create a statement for one account.
     * Equivalent to COBOL 5000-CREATE-STATEMENT + 4000-TRNXFILE-GET + 6000-WRITE-TRANS.
     */
    private void createStatement(CardXrefRecord xref, CustomerRecord customer,
                                 AccountRecord account, List<TransactionRecord> transactions) {
        // Plain text statement header (ST-LINE0 through ST-LINE13)
        statementLines.add("*".repeat(31) + "START OF STATEMENT" + "*".repeat(31));

        writeHtmlHeader(account);

        String fullName = customer.getFullName();
        String addr1 = customer.getAddrLine1() != null ? customer.getAddrLine1().trim() : "";
        String addr2 = customer.getAddrLine2() != null ? customer.getAddrLine2().trim() : "";
        String addr3 = customer.getFullAddress();

        writeHtmlNameAndDetails(fullName, addr1, addr2, addr3,
                String.valueOf(account.getAcctId()),
                account.getCurrBal().toPlainString(),
                String.valueOf(customer.getFicoCreditScore()));

        statementLines.add(fullName);
        statementLines.add(addr1);
        statementLines.add(addr2);
        statementLines.add(addr3);
        statementLines.add("-".repeat(80));
        statementLines.add(centerText("Basic Details", 80));
        statementLines.add("-".repeat(80));
        statementLines.add(String.format("Account ID         :%s", account.getAcctId()));
        statementLines.add(String.format("Current Balance    :%s", account.getCurrBal()));
        statementLines.add(String.format("FICO Score         :%s", customer.getFicoCreditScore()));
        statementLines.add("-".repeat(80));
        statementLines.add(centerText("TRANSACTION SUMMARY", 80));
        statementLines.add("-".repeat(80));
        statementLines.add(String.format("%-16s%-51s%13s", "Tran ID", "Tran Details", "Tran Amount"));
        statementLines.add("-".repeat(80));

        // Write transactions (equivalent to 4000-TRNXFILE-GET + 6000-WRITE-TRANS)
        BigDecimal totalAmt = BigDecimal.ZERO;
        for (TransactionRecord tran : transactions) {
            String desc = tran.getDescription() != null ? tran.getDescription().trim() : "";
            if (desc.length() > 49) desc = desc.substring(0, 49);

            statementLines.add(String.format("%-16s %-49s$%11.2f",
                    tran.getTranId(), desc, tran.getAmount()));

            writeHtmlTransaction(tran.getTranId(), desc, tran.getAmount());

            totalAmt = totalAmt.add(tran.getAmount());
        }

        statementLines.add("-".repeat(80));
        statementLines.add(String.format("Total EXP:%56s$%11.2f", "", totalAmt));
        statementLines.add("*".repeat(32) + "END OF STATEMENT" + "*".repeat(32));

        // HTML footer
        htmlLines.add("<tr>");
        htmlLines.add("<td colspan=\"3\" style=\"padding:0px 5px;background-color:#1d1d96b3;\">");
        htmlLines.add("<h3>End of Statement</h3>");
        htmlLines.add("</td>");
        htmlLines.add("</tr>");
        htmlLines.add("</table>");
        htmlLines.add("</body>");
        htmlLines.add("</html>");
    }

    /**
     * Write the HTML header block.
     * Equivalent to COBOL 5100-WRITE-HTML-HEADER.
     */
    private void writeHtmlHeader(AccountRecord account) {
        htmlLines.add("<!DOCTYPE html>");
        htmlLines.add("<html lang=\"en\">");
        htmlLines.add("<head>");
        htmlLines.add("<meta charset=\"utf-8\">");
        htmlLines.add("<title>HTML Table Layout</title>");
        htmlLines.add("</head>");
        htmlLines.add("<body style=\"margin:0px;\">");
        htmlLines.add("<table  align=\"center\" frame=\"box\" style=\"width:70%; font:12px Segoe UI,sans-serif;\">");
        htmlLines.add("<tr>");
        htmlLines.add("<td colspan=\"3\" style=\"padding:0px 5px;background-color:#1d1d96b3;\">");
        htmlLines.add("<h3>Statement for Account Number: " + account.getAcctId() + "</h3>");
        htmlLines.add("</td>");
        htmlLines.add("</tr>");
        htmlLines.add("<tr>");
        htmlLines.add("<td colspan=\"3\" style=\"padding:0px 5px;background-color:#FFAF33;\">");
        htmlLines.add("<p style=\"font-size:16px\">Bank of XYZ</p>");
        htmlLines.add("<p>410 Terry Ave N</p>");
        htmlLines.add("<p>Seattle WA 99999</p>");
        htmlLines.add("</td>");
        htmlLines.add("</tr>");
        htmlLines.add("<tr>");
        htmlLines.add("<td colspan=\"3\" style=\"padding:0px 5px;background-color:#f2f2f2;\">");
    }

    /**
     * Write name, address, and basic details in HTML.
     * Equivalent to COBOL 5200-WRITE-HTML-NMADBS.
     */
    private void writeHtmlNameAndDetails(String name, String addr1, String addr2, String addr3,
                                         String acctId, String currBal, String ficoScore) {
        htmlLines.add("<p style=\"font-size:16px\">" + name + "</p>");
        htmlLines.add("<p>" + addr1 + "</p>");
        htmlLines.add("<p>" + addr2 + "</p>");
        htmlLines.add("<p>" + addr3 + "</p>");
        htmlLines.add("</td>");
        htmlLines.add("</tr>");
        htmlLines.add("<tr>");
        htmlLines.add("<td colspan=\"3\" style=\"padding:0px 5px;background-color:#33FFD1; text-align:center;\">");
        htmlLines.add("<p style=\"font-size:16px\">Basic Details</p>");
        htmlLines.add("</td>");
        htmlLines.add("</tr>");
        htmlLines.add("<tr>");
        htmlLines.add("<td colspan=\"3\" style=\"padding:0px 5px;background-color:#f2f2f2;\">");
        htmlLines.add("<p>Account ID         : " + acctId + "</p>");
        htmlLines.add("<p>Current Balance    : " + currBal + "</p>");
        htmlLines.add("<p>FICO Score         : " + ficoScore + "</p>");
        htmlLines.add("</td>");
        htmlLines.add("</tr>");
        htmlLines.add("<tr>");
        htmlLines.add("<td colspan=\"3\" style=\"padding:0px 5px;background-color:#33FFD1; text-align:center;\">");
        htmlLines.add("<p style=\"font-size:16px\">Transaction Summary</p>");
        htmlLines.add("</td>");
        htmlLines.add("</tr>");

        // Transaction header row
        htmlLines.add("<tr>");
        htmlLines.add("<td style=\"width:25%; padding:0px 5px; background-color:#33FF5E; text-align:left;\">");
        htmlLines.add("<p style=\"font-size:16px\">Tran ID</p>");
        htmlLines.add("</td>");
        htmlLines.add("<td style=\"width:55%; padding:0px 5px; background-color:#33FF5E; text-align:left;\">");
        htmlLines.add("<p style=\"font-size:16px\">Tran Details</p>");
        htmlLines.add("</td>");
        htmlLines.add("<td style=\"width:20%; padding:0px 5px; background-color:#33FF5E; text-align:right;\">");
        htmlLines.add("<p style=\"font-size:16px\">Amount</p>");
        htmlLines.add("</td>");
        htmlLines.add("</tr>");
    }

    /**
     * Write a single transaction in HTML.
     * Equivalent to COBOL 6000-WRITE-TRANS HTML portion.
     */
    private void writeHtmlTransaction(String tranId, String description, BigDecimal amount) {
        htmlLines.add("<tr>");
        htmlLines.add("<td style=\"width:25%; padding:0px 5px; background-color:#f2f2f2; text-align:left;\">");
        htmlLines.add("<p>" + tranId + "</p>");
        htmlLines.add("</td>");
        htmlLines.add("<td style=\"width:55%; padding:0px 5px; background-color:#f2f2f2; text-align:left;\">");
        htmlLines.add("<p>" + description + "</p>");
        htmlLines.add("</td>");
        htmlLines.add("<td style=\"width:20%; padding:0px 5px; background-color:#f2f2f2; text-align:right;\">");
        htmlLines.add("<p>" + String.format("$%11.2f", amount) + "</p>");
        htmlLines.add("</td>");
        htmlLines.add("</tr>");
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    public List<String> getStatementLines() { return statementLines; }
    public List<String> getHtmlLines() { return htmlLines; }
}
