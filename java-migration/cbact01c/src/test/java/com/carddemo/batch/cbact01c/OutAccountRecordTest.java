package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.util.DateFormatter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link OutAccountRecord} — verifies COBOL business rules:
 * <ul>
 *   <li>Date reformatting (YYYY-MM-DD → YYYYMMDD)</li>
 *   <li>Zero-debit defaulting to 2525.00</li>
 * </ul>
 */
class OutAccountRecordTest {

    private static final String RECORD_1 =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20"
          + "00000000000{00000000000{A000000000"
          + " ".repeat(178);

    @Test
    void fromAccount_reissueDateReformatted() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        String formatted = DateFormatter.stripDashes(acct.acctReissueDate());
        OutAccountRecord out = OutAccountRecord.fromAccount(acct, formatted);
        assertEquals("20250520", out.acctReissueDate());
    }

    @Test
    void fromAccount_zeroDebitDefaultedTo2525() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        // Both cycle credit and debit are zero in record 1
        assertEquals(0, acct.acctCurrCycDebit().compareTo(BigDecimal.ZERO));

        String formatted = DateFormatter.stripDashes(acct.acctReissueDate());
        OutAccountRecord out = OutAccountRecord.fromAccount(acct, formatted);
        assertEquals(new BigDecimal("2525.00"), out.acctCurrCycDebit());
    }

    @Test
    void fromAccount_nonZeroDebitPreserved() {
        // Build an account with a non-zero debit
        AccountRecord acct = new AccountRecord(
                99L, "Y",
                new BigDecimal("100.00"), new BigDecimal("5000.00"),
                new BigDecimal("2000.00"),
                "2020-01-01", "2025-01-01", "2025-06-15",
                new BigDecimal("50.00"), new BigDecimal("75.50"),
                "12345", "GRP001"
        );
        String formatted = DateFormatter.stripDashes(acct.acctReissueDate());
        OutAccountRecord out = OutAccountRecord.fromAccount(acct, formatted);
        assertEquals(new BigDecimal("75.50"), out.acctCurrCycDebit());
    }

    @Test
    void fromAccount_fieldsPassedThrough() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        String formatted = DateFormatter.stripDashes(acct.acctReissueDate());
        OutAccountRecord out = OutAccountRecord.fromAccount(acct, formatted);

        assertEquals(1L, out.acctId());
        assertEquals("Y", out.acctActiveStatus());
        assertEquals(acct.acctCurrBal(), out.acctCurrBal());
        assertEquals(acct.acctCreditLimit(), out.acctCreditLimit());
        assertEquals(acct.acctCashCreditLimit(), out.acctCashCreditLimit());
        assertEquals(acct.acctOpenDate(), out.acctOpenDate());
        assertEquals(acct.acctExpirationDate(), out.acctExpirationDate());
        assertEquals(acct.acctCurrCycCredit(), out.acctCurrCycCredit());
        // GROUP-ID is spaces in the sample data (trimmed to empty)
        assertEquals("", out.acctGroupId());
    }

    @Test
    void toDelimitedLine_format() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        String formatted = DateFormatter.stripDashes(acct.acctReissueDate());
        OutAccountRecord out = OutAccountRecord.fromAccount(acct, formatted);
        String line = out.toDelimitedLine();

        String[] fields = line.split("\\|");
        // Last field (groupId) is empty, so split produces 10 fields
        // (trailing empty strings are dropped by String.split)
        assertTrue(fields.length >= 10, "should have at least 10 pipe-separated fields");
        assertEquals("00000000001", fields[0]);
        assertEquals("Y", fields[1]);
        assertEquals("20250520", fields[7]); // reformatted reissue date
        assertEquals("2525.00", fields[9]);  // defaulted debit
    }
}
