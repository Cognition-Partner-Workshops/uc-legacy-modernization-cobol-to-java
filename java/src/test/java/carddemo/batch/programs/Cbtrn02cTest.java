package carddemo.batch.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import carddemo.batch.programs.Cbtrn02c.Rejection;
import carddemo.batch.records.AccountRecord;
import org.junit.jupiter.api.Test;

class Cbtrn02cTest {
    private static final String TS_2022 = "2022-07-18-00.00.00.000000";
    private static final String TS_2030 = "2030-01-01-00.00.00.000000";

    private static AccountRecord account(long creditLimit, long cycCredit, long cycDebit, String expires) {
        AccountRecord a = new AccountRecord();
        a.setId("00000000001");
        a.setCreditLimit(creditLimit);
        a.setCurrCycCredit(cycCredit);
        a.setCurrCycDebit(cycDebit);
        a.setExpirationDate(expires);
        return a;
    }

    @Test
    void acceptsTransactionWithinLimitBeforeExpiry() {
        Rejection r = Cbtrn02c.validateAccount(account(100_000, 20_000, 5_000, "2025-12-31"), 50_000, TS_2022);
        assertFalse(r.isRejected());
    }

    @Test
    void rejectsOverlimitUsingCycleCreditMinusCycleDebitPlusAmount() {
        // 20000 - 5000 + 85001 = 100001 > 100000 limit
        Rejection r = Cbtrn02c.validateAccount(account(100_000, 20_000, 5_000, "2025-12-31"), 85_001, TS_2022);
        assertEquals(Rejection.OVERLIMIT, r);
        assertEquals(102, r.reason());
        // exactly at the limit is allowed (ACCT-CREDIT-LIMIT >= WS-TEMP-BAL)
        assertFalse(Cbtrn02c.validateAccount(account(100_000, 20_000, 5_000, "2025-12-31"), 85_000, TS_2022).isRejected());
    }

    @Test
    void rejectsTransactionsAfterExpiryUsingDatePortionOfTimestamp() {
        Rejection r = Cbtrn02c.validateAccount(account(100_000, 0, 0, "2029-12-31"), 100, TS_2030);
        assertEquals(Rejection.EXPIRED, r);
        assertEquals(103, r.reason());
        // expiry day itself is still accepted
        assertFalse(Cbtrn02c.validateAccount(account(100_000, 0, 0, "2030-01-01"), 100, TS_2030).isRejected());
    }

    @Test
    void expiryCheckOverridesOverlimitReason() {
        Rejection r = Cbtrn02c.validateAccount(account(100, 0, 0, "2020-01-01"), 1_000_000, TS_2022);
        assertEquals(Rejection.EXPIRED, r);
    }

    @Test
    void rejectionTrailerIsFourDigitReasonPlusPaddedDescription() {
        String trailer = Rejection.INVALID_CARD.trailer();
        assertEquals(80, trailer.length());
        assertTrue(trailer.startsWith("0100INVALID CARD NUMBER FOUND"));
        assertEquals("0103TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
                Rejection.EXPIRED.trailer().stripTrailing());
    }

    @Test
    void positiveAmountsPostToCycleCreditAndNegativeToCycleDebit() {
        AccountRecord a = account(100_000, 1_000, -500, "2025-12-31");
        a.setCurrBal(10_000);

        Cbtrn02c.applyToAccount(a, 2_500);
        assertEquals(12_500, a.currBal());
        assertEquals(3_500, a.currCycCredit());
        assertEquals(-500, a.currCycDebit());

        Cbtrn02c.applyToAccount(a, -1_200);
        assertEquals(11_300, a.currBal());
        assertEquals(3_500, a.currCycCredit());
        assertEquals(-1_700, a.currCycDebit());

        Cbtrn02c.applyToAccount(a, 0);
        assertEquals(3_500, a.currCycCredit());
    }
}
