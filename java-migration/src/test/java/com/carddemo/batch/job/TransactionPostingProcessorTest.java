package com.carddemo.batch.job;

import com.carddemo.batch.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TransactionPostingProcessorTest {

    @TempDir
    Path tempDir;

    private Path dailyTranFile;
    private Path transactOutputFile;
    private Path rejectFile;
    private Map<String, CardXrefRecord> xrefByCardNum;
    private Map<Long, AccountRecord> accountMap;
    private Map<String, TranCatBalRecord> tranCatBalMap;

    @BeforeEach
    void setUp() {
        dailyTranFile = tempDir.resolve("dailytran.txt");
        transactOutputFile = tempDir.resolve("tranout.txt");
        rejectFile = tempDir.resolve("rejects.txt");
        xrefByCardNum = new HashMap<>();
        accountMap = new HashMap<>();
        tranCatBalMap = new HashMap<>();

        // Set up xref
        CardXrefRecord xref = new CardXrefRecord();
        xref.setXrefCardNum("4111111111111111");
        xref.setXrefCustId(1L);
        xref.setXrefAcctId(1L);
        xrefByCardNum.put("4111111111111111", xref);

        // Set up account
        AccountRecord acct = new AccountRecord();
        acct.setAcctId(1L);
        acct.setAcctActiveStatus("Y");
        acct.setAcctCurrBal(new BigDecimal("10000.00"));
        acct.setAcctCreditLimit(new BigDecimal("50000.00"));
        acct.setAcctCurrCycCredit(BigDecimal.ZERO);
        acct.setAcctCurrCycDebit(BigDecimal.ZERO);
        accountMap.put(1L, acct);
    }

    @Test
    void executePostsValidTransaction() throws IOException {
        // Build daily transaction line: tranId(16) + typeCd(2) + catCd(4) + source(10) + desc(100)
        // + amount(12) + merchantId(9) + merchantName(50) + merchantCity(50) + merchantZip(10)
        // + cardNum(16) + origTs(26) + procTs(26)
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-16s", "TRN0000001"));     // tranId
        sb.append("01");                                      // typeCd
        sb.append("0005");                                    // catCd
        sb.append(String.format("%-10s", "POS"));             // source
        sb.append(String.format("%-100s", "Purchase at Store")); // desc
        sb.append(String.format("%+012.2f", 150.00));         // amount
        sb.append(String.format("%09d", 12345));              // merchantId
        sb.append(String.format("%-50s", "Test Store"));      // merchantName
        sb.append(String.format("%-50s", "Seattle"));         // merchantCity
        sb.append(String.format("%-10s", "98101"));           // merchantZip
        sb.append("4111111111111111");                        // cardNum
        sb.append(String.format("%-26s", "2025-01-15-10.30.00.000000")); // origTs
        sb.append(String.format("%-26s", ""));                // procTs

        Files.writeString(dailyTranFile, sb.toString() + "\n");

        TransactionPostingProcessor processor = new TransactionPostingProcessor(
                dailyTranFile, transactOutputFile, rejectFile,
                xrefByCardNum, accountMap, tranCatBalMap);

        List<TransactionRecord> posted = processor.execute();

        assertEquals(1, posted.size());
        assertEquals(1, processor.getTransactionCount());
        assertEquals(0, processor.getRejectCount());

        assertTrue(Files.exists(transactOutputFile));
        List<String> outLines = Files.readAllLines(transactOutputFile);
        assertEquals(1, outLines.size());

        // Check account balance updated
        AccountRecord acct = accountMap.get(1L);
        assertEquals(new BigDecimal("10150.00"), acct.getAcctCurrBal());
        assertEquals(new BigDecimal("150.00"), acct.getAcctCurrCycDebit());
    }

    @Test
    void executeRejectsTransactionWithUnknownCard() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-16s", "TRN0000002"));
        sb.append("01");
        sb.append("0005");
        sb.append(String.format("%-10s", "POS"));
        sb.append(String.format("%-100s", "Unknown card purchase"));
        sb.append(String.format("%+012.2f", 100.00));
        sb.append(String.format("%09d", 0));
        sb.append(String.format("%-50s", ""));
        sb.append(String.format("%-50s", ""));
        sb.append(String.format("%-10s", ""));
        sb.append("9999999999999999");  // Unknown card
        sb.append(String.format("%-26s", "2025-01-15-10.30.00.000000"));
        sb.append(String.format("%-26s", ""));

        Files.writeString(dailyTranFile, sb.toString() + "\n");

        TransactionPostingProcessor processor = new TransactionPostingProcessor(
                dailyTranFile, transactOutputFile, rejectFile,
                xrefByCardNum, accountMap, tranCatBalMap);

        List<TransactionRecord> posted = processor.execute();

        assertTrue(posted.isEmpty());
        assertEquals(1, processor.getTransactionCount());
        assertEquals(1, processor.getRejectCount());

        List<String> rejectLines = Files.readAllLines(rejectFile);
        assertEquals(1, rejectLines.size());
        assertTrue(rejectLines.get(0).contains("1001"));
    }

    @Test
    void executeRejectsTransactionForInactiveAccount() throws IOException {
        // Make account inactive
        accountMap.get(1L).setAcctActiveStatus("N");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-16s", "TRN0000003"));
        sb.append("01");
        sb.append("0005");
        sb.append(String.format("%-10s", "POS"));
        sb.append(String.format("%-100s", "Inactive account purchase"));
        sb.append(String.format("%+012.2f", 50.00));
        sb.append(String.format("%09d", 0));
        sb.append(String.format("%-50s", ""));
        sb.append(String.format("%-50s", ""));
        sb.append(String.format("%-10s", ""));
        sb.append("4111111111111111");
        sb.append(String.format("%-26s", "2025-01-15-10.30.00.000000"));
        sb.append(String.format("%-26s", ""));

        Files.writeString(dailyTranFile, sb.toString() + "\n");

        TransactionPostingProcessor processor = new TransactionPostingProcessor(
                dailyTranFile, transactOutputFile, rejectFile,
                xrefByCardNum, accountMap, tranCatBalMap);

        List<TransactionRecord> posted = processor.execute();

        assertTrue(posted.isEmpty());
        assertEquals(1, processor.getRejectCount());

        List<String> rejectLines = Files.readAllLines(rejectFile);
        assertTrue(rejectLines.get(0).contains("1003"));
    }

    @Test
    void executeHandlesEmptyFile() throws IOException {
        Files.writeString(dailyTranFile, "");

        TransactionPostingProcessor processor = new TransactionPostingProcessor(
                dailyTranFile, transactOutputFile, rejectFile,
                xrefByCardNum, accountMap, tranCatBalMap);

        List<TransactionRecord> posted = processor.execute();
        assertTrue(posted.isEmpty());
        assertEquals(0, processor.getTransactionCount());
    }

    @Test
    void updateAccountBalanceHandlesCreditTransaction() {
        TransactionPostingProcessor processor = new TransactionPostingProcessor(
                dailyTranFile, transactOutputFile, rejectFile,
                xrefByCardNum, accountMap, tranCatBalMap);

        AccountRecord acct = accountMap.get(1L);
        TransactionRecord tran = new TransactionRecord();
        tran.setTranAmt(new BigDecimal("-200.00")); // credit (negative)

        processor.updateAccountBalance(acct, tran);

        assertEquals(new BigDecimal("9800.00"), acct.getAcctCurrBal());
        assertEquals(new BigDecimal("200.00"), acct.getAcctCurrCycCredit());
        assertEquals(BigDecimal.ZERO, acct.getAcctCurrCycDebit());
    }

    @Test
    void updateTranCatBalanceCreatesNewRecordIfMissing() {
        TransactionPostingProcessor processor = new TransactionPostingProcessor(
                dailyTranFile, transactOutputFile, rejectFile,
                xrefByCardNum, accountMap, tranCatBalMap);

        TransactionRecord tran = new TransactionRecord();
        tran.setTranTypeCd("01");
        tran.setTranCatCd(5);
        tran.setTranAmt(new BigDecimal("100.00"));

        processor.updateTranCatBalance(1L, tran);

        assertFalse(tranCatBalMap.isEmpty());
        assertEquals(1, tranCatBalMap.size());
    }

    @Test
    void validateTransactionReturnsValidForGoodData() {
        TransactionPostingProcessor processor = new TransactionPostingProcessor(
                dailyTranFile, transactOutputFile, rejectFile,
                xrefByCardNum, accountMap, tranCatBalMap);

        TransactionRecord tran = new TransactionRecord();
        tran.setTranCardNum("4111111111111111");

        TransactionPostingProcessor.ValidationResult result = processor.validateTransaction(tran);
        assertTrue(result.isValid());
        assertEquals(0, result.failReasonCode());
    }
}
