package com.carddemo.statement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Java port of the COBOL batch program {@code app/cbl/CBSTM03A.CBL}.
 *
 * <p>Pass 1 groups the sorted transaction file per card number ({@link TransactionTable}).
 * Pass 2 walks XREFFILE in ascending card-number order, reads CUSTFILE by customer id and
 * ACCTFILE by account id, and writes the statement records in exactly the order of the
 * COBOL {@code WRITE} statements, including the intentionally repeated separator lines.
 *
 * <p>The mainframe-only PSA/TIOT control-block inspection of the COBOL program produces
 * DISPLAY output only and is therefore not ported.
 */
public final class StatementGenerator {

    private static final String STARS_31 = "*".repeat(31);
    private static final String STARS_32 = "*".repeat(32);
    private static final String DASHES_80 = "-".repeat(80);
    /** {@code WS-TOTAL-AMT PIC S9(9)V99}: 11 digits, so 9 integer positions. */
    private static final BigInteger TOTAL_MODULUS = BigInteger.TEN.pow(11);

    private final CardDemoData data;

    public StatementGenerator(CardDemoData data) {
        this.data = data;
    }

    /** Runs the equivalent of {@code 1000-MAINLINE}, one statement per XREF record. */
    public List<AccountStatement> generate() {
        TransactionTable table = TransactionTable.build(data.transactions());
        List<AccountStatement> statements = new ArrayList<>();
        for (CardXrefRecord xref : data.xrefRecords()) {
            CustomerRecord customer = data.customer(xref.customerId());
            AccountRecord account = data.account(xref.accountId());
            statements.add(createStatement(xref, customer, account, table));
        }
        return statements;
    }

    private AccountStatement createStatement(CardXrefRecord xref,
                                             CustomerRecord customer,
                                             AccountRecord account,
                                             TransactionTable table) {
        List<String> text = new ArrayList<>();
        List<String> html = new ArrayList<>();

        // 5000-CREATE-STATEMENT
        String stName = CobolText.alphanumeric(concatDelimitedBySpace(
                customer.firstName(), customer.middleName(), customer.lastName()), 75);
        String stAdd1 = CobolText.alphanumeric(customer.addressLine1(), 50);
        String stAdd2 = CobolText.alphanumeric(customer.addressLine2(), 50);
        String stAdd3 = CobolText.alphanumeric(concatDelimitedBySpace(
                customer.addressLine3(), customer.stateCode(),
                customer.countryCode(), customer.zip()), 80);
        String stAcctId = CobolText.alphanumeric(account.accountId(), 20);
        String stCurrBal = PicEdit.zeroFilled(account.currentBalance());
        String stFicoScore = CobolText.alphanumeric(customer.ficoCreditScore(), 20);

        writeText(text, STARS_31 + "START OF STATEMENT" + STARS_31);
        writeHtmlHeader(html, stAcctId);
        writeHtmlNameAddressBasics(html, stName, stAdd1, stAdd2, stAdd3, stAcctId, stCurrBal, stFicoScore);

        writeText(text, stName + " ".repeat(5));
        writeText(text, stAdd1 + " ".repeat(30));
        writeText(text, stAdd2 + " ".repeat(30));
        writeText(text, stAdd3);
        writeText(text, DASHES_80);
        writeText(text, " ".repeat(33) + CobolText.alphanumeric("Basic Details", 14) + " ".repeat(33));
        writeText(text, DASHES_80);
        writeText(text, "Account ID         :" + stAcctId + " ".repeat(40));
        writeText(text, "Current Balance    :" + stCurrBal + " ".repeat(47));
        writeText(text, "FICO Score         :" + stFicoScore + " ".repeat(40));
        writeText(text, DASHES_80);
        writeText(text, " ".repeat(30) + "TRANSACTION SUMMARY " + " ".repeat(30));
        writeText(text, DASHES_80);
        writeText(text, "Tran ID         " + CobolText.alphanumeric("Tran Details    ", 51) + "  Tran Amount");
        writeText(text, DASHES_80);

        // 4000-TRNXFILE-GET
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        int transactionCount = 0;
        for (int card = 1; card <= table.cardCount(); card++) {
            if (table.cardNumber(card).compareTo(xref.cardNumber()) > 0) {
                break;
            }
            if (!xref.cardNumber().equals(table.cardNumber(card))) {
                continue;
            }
            for (int tran = 1; tran <= table.transactionCount(card); tran++) {
                TransactionRecord transaction = new TransactionRecord(
                        table.cardNumber(card),
                        table.transactionId(card, tran),
                        table.transactionRest(card, tran));
                writeTransaction(text, html, transaction);
                transactionCount++;
                total = truncateTotal(total.add(transaction.amount()));
            }
        }

        writeText(text, DASHES_80);
        writeText(text, "Total EXP:" + " ".repeat(56) + "$" + PicEdit.zeroSuppressed(total));
        writeText(text, STARS_32 + "END OF STATEMENT" + STARS_32);

        writeHtml(html, HtmlLines.TRS);
        writeHtml(html, HtmlLines.L10);
        writeHtml(html, HtmlLines.L75);
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.TRE);
        writeHtml(html, HtmlLines.L78);
        writeHtml(html, HtmlLines.L79);
        writeHtml(html, HtmlLines.L80);

        return new AccountStatement(account.accountId(), stName.trim(),
                transactionCount, total, List.copyOf(text), List.copyOf(html));
    }

    /** 6000-WRITE-TRANS. */
    private void writeTransaction(List<String> text, List<String> html, TransactionRecord transaction) {
        String stTranId = CobolText.alphanumeric(transaction.transactionId(), 16);
        String stTranDt = CobolText.alphanumeric(transaction.description(), 49);
        String stTranAmt = PicEdit.zeroSuppressed(transaction.amount());

        writeText(text, stTranId + " " + stTranDt + "$" + stTranAmt);

        writeHtml(html, HtmlLines.TRS);
        writeHtml(html, HtmlLines.L58);
        writeHtml(html, "<p>" + stTranId + "</p>");
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.L61);
        writeHtml(html, "<p>" + stTranDt + "</p>");
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.L64);
        writeHtml(html, "<p>" + stTranAmt + "</p>");
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.TRE);
    }

    /** 5100-WRITE-HTML-HEADER. */
    private void writeHtmlHeader(List<String> html, String stAcctId) {
        writeHtml(html, HtmlLines.L01);
        writeHtml(html, HtmlLines.L02);
        writeHtml(html, HtmlLines.L03);
        writeHtml(html, HtmlLines.L04);
        writeHtml(html, HtmlLines.L05);
        writeHtml(html, HtmlLines.L06);
        writeHtml(html, HtmlLines.L07);
        writeHtml(html, HtmlLines.L08);
        writeHtml(html, HtmlLines.TRS);
        writeHtml(html, HtmlLines.L10);
        writeHtml(html, "<h3>Statement for Account Number: " + stAcctId + "</h3>");
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.TRE);
        writeHtml(html, HtmlLines.TRS);
        writeHtml(html, HtmlLines.L15);
        writeHtml(html, HtmlLines.L16);
        writeHtml(html, HtmlLines.L17);
        writeHtml(html, HtmlLines.L18);
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.TRE);
        writeHtml(html, HtmlLines.TRS);
        writeHtml(html, HtmlLines.L22_35);
    }

    /** 5200-WRITE-HTML-NMADBS. */
    private void writeHtmlNameAddressBasics(List<String> html,
                                            String stName,
                                            String stAdd1,
                                            String stAdd2,
                                            String stAdd3,
                                            String stAcctId,
                                            String stCurrBal,
                                            String stFicoScore) {
        String l23Name = CobolText.alphanumeric(stName, 50);
        writeHtml(html, "<p style=\"font-size:16px\">" + CobolText.delimitedBy(l23Name, "  ") + "  </p>");
        writeHtml(html, "<p>" + CobolText.delimitedBy(stAdd1, "  ") + "  </p>");
        writeHtml(html, "<p>" + CobolText.delimitedBy(stAdd2, "  ") + "  </p>");
        writeHtml(html, "<p>" + CobolText.delimitedBy(stAdd3, "  ") + "  </p>");

        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.TRE);
        writeHtml(html, HtmlLines.TRS);
        writeHtml(html, HtmlLines.L30_42);
        writeHtml(html, HtmlLines.L31);
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.TRE);
        writeHtml(html, HtmlLines.TRS);
        writeHtml(html, HtmlLines.L22_35);

        writeHtml(html, "<p>Account ID         : " + stAcctId + "</p>");
        writeHtml(html, "<p>Current Balance    : " + stCurrBal + "</p>");
        writeHtml(html, "<p>FICO Score         : " + stFicoScore + "</p>");

        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.TRE);
        writeHtml(html, HtmlLines.TRS);
        writeHtml(html, HtmlLines.L30_42);
        writeHtml(html, HtmlLines.L43);
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.TRE);
        writeHtml(html, HtmlLines.TRS);
        writeHtml(html, HtmlLines.L47);
        writeHtml(html, HtmlLines.L48);
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.L50);
        writeHtml(html, HtmlLines.L51);
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.L53);
        writeHtml(html, HtmlLines.L54);
        writeHtml(html, HtmlLines.TDE);
        writeHtml(html, HtmlLines.TRE);
    }

    /**
     * {@code STRING token DELIMITED BY ' ' ' ' DELIMITED BY SIZE ...}: every source item
     * is truncated at its first space and the parts are joined with single spaces, which
     * also leaves the trailing space the COBOL STRING emits.
     */
    static String concatDelimitedBySpace(String... sources) {
        StringBuilder result = new StringBuilder();
        for (String source : sources) {
            result.append(CobolText.delimitedBy(source, " ")).append(' ');
        }
        return result.toString();
    }

    private static BigDecimal truncateTotal(BigDecimal total) {
        BigInteger cents = total.unscaledValue();
        BigInteger truncated = cents.abs().mod(TOTAL_MODULUS);
        return new BigDecimal(cents.signum() < 0 ? truncated.negate() : truncated, 2);
    }

    private static void writeText(List<String> text, String record) {
        text.add(CobolText.alphanumeric(record, AccountStatement.TEXT_RECORD_LENGTH));
    }

    private static void writeHtml(List<String> html, String record) {
        html.add(CobolText.alphanumeric(record, AccountStatement.HTML_RECORD_LENGTH));
    }
}
