package carddemo.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import carddemo.programs.Cbact01c.ArrayRecord;
import carddemo.programs.Cbact01c.OutRecord;
import carddemo.programs.Cbact01c.Vbrc1;
import carddemo.programs.Cbact01c.Vbrc2;
import carddemo.records.AccountRecord;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Cbact01cTest {

    private static AccountRecord account(String cycDebit) {
        AccountRecord acct = new AccountRecord();
        acct.set(AccountRecord.ACCT_ID, 1);
        acct.set(AccountRecord.ACTIVE_STATUS, "Y");
        acct.set(AccountRecord.CURR_BAL, new BigDecimal("194.00"));
        acct.set(AccountRecord.CREDIT_LIMIT, new BigDecimal("2020.00"));
        acct.set(AccountRecord.CASH_CREDIT_LIMIT, new BigDecimal("1020.00"));
        acct.set(AccountRecord.CURR_CYC_CREDIT, BigDecimal.ZERO);
        acct.set(AccountRecord.REISSUE_DATE, "2025-05-20");
        acct.set(AccountRecord.CURR_CYC_DEBIT, new BigDecimal(cycDebit));
        return acct;
    }

    @Test
    void dateFormatterDropsAndRestoresDashes() {
        assertEquals("20250520", DateFormatter.dashedToCompact("2025-05-20"));
        assertEquals("2025-05-20", DateFormatter.compactToDashed("20250520"));
    }

    @Test
    void outRecordUsesCompactReissueDateAndOnlyStoresDebitWhenAccountDebitIsZero() {
        OutRecord out = new OutRecord();
        Cbact01c.populateOutRecord(account("0.00"), out);
        assertEquals("20250520", out.getText(OutRecord.REISSUE_DATE).stripTrailing());
        assertEquals(new BigDecimal("2525.00"), out.get(OutRecord.CURR_CYC_DEBIT));

        // A non-zero account debit is never moved, so the previous output value is left in place.
        Cbact01c.populateOutRecord(account("-12.50"), out);
        assertEquals(new BigDecimal("2525.00"), out.get(OutRecord.CURR_CYC_DEBIT));
    }

    @Test
    void arrayRecordFillsThreeSlotsAndLeavesRestInitialized() {
        ArrayRecord array = new ArrayRecord();
        array.initialize();
        Cbact01c.populateArrayRecord(account("0.00"), array);
        assertEquals(new BigDecimal("194.00"), array.get(ArrayRecord.currBal(1)));
        assertEquals(new BigDecimal("1005.00"), array.get(ArrayRecord.currCycDebit(1)));
        assertEquals(new BigDecimal("194.00"), array.get(ArrayRecord.currBal(2)));
        assertEquals(new BigDecimal("1525.00"), array.get(ArrayRecord.currCycDebit(2)));
        assertEquals(new BigDecimal("-1025.00"), array.get(ArrayRecord.currBal(3)));
        assertEquals(new BigDecimal("-2500.00"), array.get(ArrayRecord.currCycDebit(3)));
        assertEquals(new BigDecimal("0.00"), array.get(ArrayRecord.currBal(4)));
        assertEquals(new BigDecimal("0.00"), array.get(ArrayRecord.currCycDebit(5)));
    }

    @Test
    void variableRecordsCarryStatusAndReissueYear() {
        Vbrc1 v1 = new Vbrc1();
        Vbrc2 v2 = new Vbrc2();
        Cbact01c.populateVbrcRecords(account("0.00"), v1, v2);
        assertEquals("00000000001Y", new String(v1.bytes(), java.nio.charset.StandardCharsets.US_ASCII));
        assertEquals("2025", v2.getText(Vbrc2.REISSUE_YYYY));
        assertEquals(new BigDecimal("2020.00"), v2.get(Vbrc2.CREDIT_LIMIT));
    }
}
