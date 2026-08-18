package com.carddemo.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecordParsingTest {

    /** First line of {@code app/data/ASCII/cardxref.txt} (36 chars, padded to RECLN 50 on read). */
    private static final String XREF_LINE = "050002445376574000000005000000000050";

    @Test
    void parsesCardXrefRecord() {
        CardXrefRecord xref = CardXrefRecord.parse(XREF_LINE);
        assertEquals("0500024453765740", xref.cardNumber());
        assertEquals("000000050", xref.customerId());
        assertEquals("00000000050", xref.accountId());
    }

    @Test
    void parsesCustomerRecord() {
        StringBuilder rec = new StringBuilder();
        rec.append("000000050");
        rec.append(CobolText.alphanumeric("Aniya", 25));
        rec.append(CobolText.alphanumeric("Alba", 25));
        rec.append(CobolText.alphanumeric("Von", 25));
        rec.append(CobolText.alphanumeric("1588 Nienow Cape", 50));
        rec.append(CobolText.alphanumeric("Suite 187", 50));
        rec.append(CobolText.alphanumeric("New Enoshaven", 50));
        rec.append("OR");
        rec.append("USA");
        rec.append(CobolText.alphanumeric("04257", 10));
        rec.append(CobolText.alphanumeric("", 15 + 15 + 9 + 20 + 10 + 10 + 1));
        rec.append("623");

        CustomerRecord customer = CustomerRecord.parse(rec.toString());
        assertEquals("000000050", customer.customerId());
        assertEquals(CobolText.alphanumeric("Aniya", 25), customer.firstName());
        assertEquals(CobolText.alphanumeric("Alba", 25), customer.middleName());
        assertEquals(CobolText.alphanumeric("Von", 25), customer.lastName());
        assertEquals(CobolText.alphanumeric("1588 Nienow Cape", 50), customer.addressLine1());
        assertEquals(CobolText.alphanumeric("Suite 187", 50), customer.addressLine2());
        assertEquals(CobolText.alphanumeric("New Enoshaven", 50), customer.addressLine3());
        assertEquals("OR", customer.stateCode());
        assertEquals("USA", customer.countryCode());
        assertEquals(CobolText.alphanumeric("04257", 10), customer.zip());
        assertEquals("623", customer.ficoCreditScore());
    }

    @Test
    void parsesAccountRecordWithOverpunchedBalance() {
        // first line of app/data/ASCII/acctdata.txt: ACCT-CURR-BAL is S9(10)V99 with a '{' (+0) overpunch
        String rec = "00000000050" + "Y" + "00000004920{";
        AccountRecord account = AccountRecord.parse(rec);
        assertEquals("00000000050", account.accountId());
        assertEquals("Y", account.activeStatus());
        assertEquals(new BigDecimal("492.00"), account.currentBalance());
    }

    @Test
    void reshapesTransactionRecordFromTheJclOutrecLayout() {
        String source = CobolText.alphanumeric("", TransactionRecord.RECORD_LENGTH);
        source = replace(source, 0, "0000000000683580");           // TRAN-ID
        source = replace(source, 16, "01");                        // TYPE-CD
        source = replace(source, 18, "0001");                      // CAT-CD
        source = replace(source, 22, "POS TERM  ");                // SOURCE
        source = replace(source, 32, CobolText.alphanumeric("Purchase at Abshire-Lowe", 100));
        source = replace(source, 132, "0000005047G");              // AMT
        source = replace(source, 262, "4444333322221111");         // CARD-NUM

        TransactionRecord transaction = TransactionRecord.parse(JclTransactionSort.reshape(source));
        assertEquals("4444333322221111", transaction.cardNumber());
        assertEquals("0000000000683580", transaction.transactionId());
        assertEquals("01", transaction.typeCode());
        assertEquals("0001", transaction.categoryCode());
        assertEquals("POS TERM  ", transaction.source());
        assertEquals(CobolText.alphanumeric("Purchase at Abshire-Lowe", 100), transaction.description());
        assertEquals(new BigDecimal("504.77"), transaction.amount());
    }

    @Test
    void sortsByCardNumberThenTransactionId() {
        String a = record("2222222222222222", "0000000000000002");
        String b = record("1111111111111111", "0000000000000009");
        String c = record("1111111111111111", "0000000000000001");

        List<TransactionRecord> sorted = JclTransactionSort.reshapeAndSort(List.of(a, b, c));
        assertEquals(List.of("1111111111111111", "1111111111111111", "2222222222222222"),
                sorted.stream().map(TransactionRecord::cardNumber).toList());
        assertEquals(List.of("0000000000000001", "0000000000000009", "0000000000000002"),
                sorted.stream().map(TransactionRecord::transactionId).toList());
    }

    private static String record(String cardNumber, String transactionId) {
        String rec = CobolText.alphanumeric("", TransactionRecord.RECORD_LENGTH);
        rec = replace(rec, 0, transactionId);
        rec = replace(rec, 132, "0000000000{");
        return replace(rec, 262, cardNumber);
    }

    private static String replace(String record, int offset, String value) {
        return record.substring(0, offset) + value + record.substring(offset + value.length());
    }
}
