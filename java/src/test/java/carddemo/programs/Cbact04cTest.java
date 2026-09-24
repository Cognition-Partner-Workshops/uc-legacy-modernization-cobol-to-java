package carddemo.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import carddemo.records.AccountRecord;
import carddemo.records.CardXrefRecord;
import carddemo.records.TranRecord;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Cbact04cTest {

    @Test
    void monthlyInterestTruncatesToTwoDecimalsWithoutRounding() {
        // 1234.56 * 15.25 / 1200 = 15.689...
        assertEquals(new BigDecimal("15.68"),
                Cbact04c.monthlyInterest(new BigDecimal("1234.56"), new BigDecimal("15.25")));
        // 0.99 * 12.00 / 1200 = 0.0099 -> 0.00
        assertEquals(new BigDecimal("0.00"),
                Cbact04c.monthlyInterest(new BigDecimal("0.99"), new BigDecimal("12.00")));
    }

    @Test
    void monthlyInterestTruncatesTowardZeroForNegativeBalances() {
        // -1000.00 * 19.99 / 1200 = -16.658... -> -16.65 (not -16.66)
        assertEquals(new BigDecimal("-16.65"),
                Cbact04c.monthlyInterest(new BigDecimal("-1000.00"), new BigDecimal("19.99")));
    }

    @Test
    void zeroRateYieldsZeroInterest() {
        assertEquals(new BigDecimal("0.00"),
                Cbact04c.monthlyInterest(new BigDecimal("5000.00"), BigDecimal.ZERO));
    }

    @Test
    void applyInterestAddsTotalAndResetsCycleCounters() {
        AccountRecord acct = new AccountRecord();
        acct.set(AccountRecord.CURR_BAL, new BigDecimal("1945.87"));
        acct.set(AccountRecord.CURR_CYC_CREDIT, new BigDecimal("1501.75"));
        acct.set(AccountRecord.CURR_CYC_DEBIT, new BigDecimal("-47.80"));
        Cbact04c.applyInterest(acct, new BigDecimal("18.77"));
        assertEquals(new BigDecimal("1964.64"), acct.get(AccountRecord.CURR_BAL));
        assertEquals(new BigDecimal("0.00"), acct.get(AccountRecord.CURR_CYC_CREDIT));
        assertEquals(new BigDecimal("0.00"), acct.get(AccountRecord.CURR_CYC_DEBIT));
    }

    @Test
    void interestTransactionLayout() {
        AccountRecord acct = new AccountRecord();
        acct.set(AccountRecord.ACCT_ID, 12);
        CardXrefRecord xref = new CardXrefRecord();
        xref.set(CardXrefRecord.CARD_NUM, "4000000000000012");
        TranRecord tran = new TranRecord();
        Cbact04c.buildInterestTransaction(tran, "2022071800", 7, acct, xref, new BigDecimal("3.21"),
                "2022-07-18-00.00.00.000000");

        assertEquals("2022071800000007", tran.getText(TranRecord.ID));
        assertEquals("01", tran.getText(TranRecord.TYPE_CD));
        assertEquals("0005", tran.getText(TranRecord.CAT_CD_TEXT));
        assertEquals("System    ", tran.getText(TranRecord.SOURCE));
        assertEquals("Int. for a/c 00000000012", tran.getText(TranRecord.DESC).stripTrailing());
        assertEquals(new BigDecimal("3.21"), tran.get(TranRecord.AMT));
        assertEquals("000000000", tran.display(TranRecord.MERCHANT_ID));
        assertEquals("4000000000000012", tran.getText(TranRecord.CARD_NUM));
        assertEquals("2022-07-18-00.00.00.000000", tran.getText(TranRecord.ORIG_TS));
        assertEquals("2022-07-18-00.00.00.000000", tran.getText(TranRecord.PROC_TS));
    }
}
