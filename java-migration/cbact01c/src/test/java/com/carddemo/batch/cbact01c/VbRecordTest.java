package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.VbRecord1;
import com.carddemo.batch.cbact01c.model.VbRecord2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link VbRecord1} and {@link VbRecord2} — the variable-length
 * record types written to VBRCFILE.
 */
class VbRecordTest {

    private static final String RECORD_1 =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20"
          + "00000000000{00000000000{A000000000"
          + " ".repeat(178);

    // ---------------------------------------------------------------
    // VB Record 1 (short: account ID + active status)
    // ---------------------------------------------------------------

    @Test
    void vb1_acctId() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        VbRecord1 vb1 = VbRecord1.fromAccount(acct);
        assertEquals(1L, vb1.acctId());
    }

    @Test
    void vb1_activeStatus() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        VbRecord1 vb1 = VbRecord1.fromAccount(acct);
        assertEquals("Y", vb1.acctActiveStatus());
    }

    @Test
    void vb1_toDelimitedLine() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        VbRecord1 vb1 = VbRecord1.fromAccount(acct);
        assertEquals("00000000001|Y", vb1.toDelimitedLine());
    }

    // ---------------------------------------------------------------
    // VB Record 2 (long: account ID + bal + limit + reissue year)
    // ---------------------------------------------------------------

    @Test
    void vb2_acctId() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        VbRecord2 vb2 = VbRecord2.fromAccount(acct);
        assertEquals(1L, vb2.acctId());
    }

    @Test
    void vb2_currBal() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        VbRecord2 vb2 = VbRecord2.fromAccount(acct);
        assertEquals(acct.acctCurrBal(), vb2.acctCurrBal());
    }

    @Test
    void vb2_creditLimit() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        VbRecord2 vb2 = VbRecord2.fromAccount(acct);
        assertEquals(acct.acctCreditLimit(), vb2.acctCreditLimit());
    }

    @Test
    void vb2_reissueYear() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        VbRecord2 vb2 = VbRecord2.fromAccount(acct);
        assertEquals("2025", vb2.acctReissueYear());
    }

    @Test
    void vb2_toDelimitedLine() {
        AccountRecord acct = AccountRecord.parse(RECORD_1);
        VbRecord2 vb2 = VbRecord2.fromAccount(acct);
        String line = vb2.toDelimitedLine();
        String[] fields = line.split("\\|");
        assertEquals(4, fields.length);
        assertEquals("00000000001", fields[0]);
        assertEquals("2025", fields[3]);
    }
}
