package carddemo.programs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import carddemo.programs.Cbtrn02c.Validation;
import carddemo.records.AccountRecord;
import carddemo.records.TranRecord;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class Cbtrn02cTest {

    private static AccountRecord account(String creditLimit, String cycCredit, String cycDebit, String expires) {
        AccountRecord acct = new AccountRecord();
        acct.set(AccountRecord.ACCT_ID, 50);
        acct.set(AccountRecord.CURR_BAL, new BigDecimal("1000.00"));
        acct.set(AccountRecord.CREDIT_LIMIT, new BigDecimal(creditLimit));
        acct.set(AccountRecord.CURR_CYC_CREDIT, new BigDecimal(cycCredit));
        acct.set(AccountRecord.CURR_CYC_DEBIT, new BigDecimal(cycDebit));
        acct.set(AccountRecord.EXPIRATION_DATE, expires);
        return acct;
    }

    private static TranRecord daily(String amount, String origTs) {
        TranRecord tran = new TranRecord();
        tran.set(TranRecord.ID, "TR00000000000001");
        tran.set(TranRecord.TYPE_CD, "02");
        tran.set(TranRecord.CAT_CD, 3001);
        tran.set(TranRecord.AMT, new BigDecimal(amount));
        tran.set(TranRecord.MERCHANT_ID, 123456789);
        tran.set(TranRecord.CARD_NUM, "4000000000000050");
        tran.set(TranRecord.ORIG_TS, origTs);
        return tran;
    }

    @Test
    void acceptsWhenWithinLimitAndBeforeExpiration() {
        Validation v = Cbtrn02c.validateAccount(
                account("5000.00", "100.00", "50.00", "2025-05-20"),
                daily("200.00", "2022-07-01-10.00.00.000000"));
        assertEquals(Validation.OK, v);
    }

    @Test
    void overLimitUsesCycleCreditMinusDebitPlusAmount() {
        // 4900 - 0 + 100 = 5000 is not over a 5000 limit; 5000.01 is.
        assertEquals(Validation.OK, Cbtrn02c.validateAccount(
                account("5000.00", "4900.00", "0.00", "2025-05-20"),
                daily("100.00", "2022-07-01-10.00.00.000000")));
        assertEquals(Validation.OVERLIMIT, Cbtrn02c.validateAccount(
                account("5000.00", "4900.00", "0.00", "2025-05-20"),
                daily("100.01", "2022-07-01-10.00.00.000000")));
        // debits lower the temporary balance
        assertEquals(Validation.OK, Cbtrn02c.validateAccount(
                account("5000.00", "4900.00", "500.00", "2025-05-20"),
                daily("600.00", "2022-07-01-10.00.00.000000")));
    }

    @Test
    void expiredAccountComparesFirstTenCharsOfOriginTimestamp() {
        assertEquals(Validation.EXPIRED, Cbtrn02c.validateAccount(
                account("5000.00", "0.00", "0.00", "2022-06-30"),
                daily("1.00", "2022-07-01-00.00.00.000000")));
        assertEquals(Validation.OK, Cbtrn02c.validateAccount(
                account("5000.00", "0.00", "0.00", "2022-07-01"),
                daily("1.00", "2022-07-01-23.59.59.999999")));
    }

    @Test
    void expirationCheckOverridesOverLimitReason() {
        Validation v = Cbtrn02c.validateAccount(
                account("10.00", "0.00", "0.00", "2020-01-01"),
                daily("999.00", "2022-07-01-00.00.00.000000"));
        assertEquals(Validation.EXPIRED, v);
        assertEquals(103, v.reason());
    }

    @Test
    void postingAddsToBalanceAndToCreditOrDebitByAmountSign() {
        AccountRecord acct = account("5000.00", "100.00", "-50.00", "2025-05-20");
        Cbtrn02c.postToAccount(acct, new BigDecimal("25.50"));
        assertEquals(new BigDecimal("1025.50"), acct.get(AccountRecord.CURR_BAL));
        assertEquals(new BigDecimal("125.50"), acct.get(AccountRecord.CURR_CYC_CREDIT));
        assertEquals(new BigDecimal("-50.00"), acct.get(AccountRecord.CURR_CYC_DEBIT));

        Cbtrn02c.postToAccount(acct, new BigDecimal("-10.00"));
        assertEquals(new BigDecimal("1015.50"), acct.get(AccountRecord.CURR_BAL));
        assertEquals(new BigDecimal("125.50"), acct.get(AccountRecord.CURR_CYC_CREDIT));
        assertEquals(new BigDecimal("-60.00"), acct.get(AccountRecord.CURR_CYC_DEBIT));
    }

    @Test
    void rejectRecordIsInputPlusFourDigitReasonAndPaddedDescription() {
        TranRecord in = daily("1.00", "2022-07-01-00.00.00.000000");
        byte[] reject = Cbtrn02c.rejectRecord(in, Validation.INVALID_CARD);
        assertEquals(430, reject.length);
        assertArrayEquals(in.bytes(), Arrays.copyOfRange(reject, 0, 350));
        String trailer = new String(reject, 350, 80, StandardCharsets.US_ASCII);
        assertEquals("0100INVALID CARD NUMBER FOUND", trailer.stripTrailing());
        assertEquals(80, trailer.length());
        assertEquals("0103", new String(Cbtrn02c.rejectRecord(in, Validation.EXPIRED), 350, 4,
                StandardCharsets.US_ASCII));
    }

    @Test
    void postedTransactionCopiesDailyFieldsAndStampsProcessingTime() {
        TranRecord in = daily("12.34", "2022-07-01-00.00.00.000000");
        in.set(TranRecord.MERCHANT_NAME, "Shop");
        TranRecord out = new TranRecord();
        Cbtrn02c.buildPostedTransaction(out, in, "2022-07-18-00.00.00.000000");
        assertEquals("TR00000000000001", out.getText(TranRecord.ID));
        assertEquals(new BigDecimal("12.34"), out.get(TranRecord.AMT));
        assertEquals("Shop", out.getText(TranRecord.MERCHANT_NAME).stripTrailing());
        assertEquals("2022-07-01-00.00.00.000000", out.getText(TranRecord.ORIG_TS));
        assertEquals("2022-07-18-00.00.00.000000", out.getText(TranRecord.PROC_TS));
    }
}
