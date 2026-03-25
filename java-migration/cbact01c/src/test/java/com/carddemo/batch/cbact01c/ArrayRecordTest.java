package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link ArrayRecord} — verifies the COBOL array population logic
 * from paragraph {@code 1400-POPUL-ARRAY-RECORD}.
 */
class ArrayRecordTest {

    private static final String RECORD_1 =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20"
          + "00000000000{00000000000{A000000000"
          + " ".repeat(178);

    @Test
    void fromAccount_acctId() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        assertEquals(1L, arr.acctId());
    }

    @Test
    void fromAccount_slot1_balanceFromInput() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        assertEquals(acct.acctCurrBal(), arr.balances()[0]);
    }

    @Test
    void fromAccount_slot1_debit1005() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        assertEquals(new BigDecimal("1005.00"), arr.debits()[0]);
    }

    @Test
    void fromAccount_slot2_balanceFromInput() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        assertEquals(acct.acctCurrBal(), arr.balances()[1]);
    }

    @Test
    void fromAccount_slot2_debit1525() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        assertEquals(new BigDecimal("1525.00"), arr.debits()[1]);
    }

    @Test
    void fromAccount_slot3_negativeBalance() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        assertEquals(new BigDecimal("-1025.00"), arr.balances()[2]);
    }

    @Test
    void fromAccount_slot3_negativeDebit() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        assertEquals(new BigDecimal("-2500.00"), arr.debits()[2]);
    }

    @Test
    void fromAccount_slot4_zeros() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        assertEquals(BigDecimal.ZERO, arr.balances()[3]);
        assertEquals(BigDecimal.ZERO, arr.debits()[3]);
    }

    @Test
    void fromAccount_slot5_zeros() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        assertEquals(BigDecimal.ZERO, arr.balances()[4]);
        assertEquals(BigDecimal.ZERO, arr.debits()[4]);
    }

    @Test
    void toDelimitedLine_format() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        ArrayRecord arr = ArrayRecord.fromAccount(acct);
        String line = arr.toDelimitedLine();

        // acctId + 5*(balance|debit) = 1 + 10 = 11 fields
        String[] fields = line.split("\\|");
        assertEquals(11, fields.length);
        assertEquals("00000000001", fields[0]);
        assertEquals("1005.00", fields[2]);  // slot 1 debit
        assertEquals("1525.00", fields[4]);  // slot 2 debit
        assertEquals("-1025.00", fields[5]); // slot 3 balance
        assertEquals("-2500.00", fields[6]); // slot 3 debit
        assertEquals("0", fields[7]);        // slot 4 balance
    }
}
