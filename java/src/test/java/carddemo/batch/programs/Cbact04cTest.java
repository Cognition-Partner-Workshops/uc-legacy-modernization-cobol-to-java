package carddemo.batch.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Cbact04cTest {

    @Test
    void monthlyInterestTruncatesTowardZero() {
        // 1000.00 * 15.00% / 1200 = 12.50 exactly
        assertEquals(1250, Cbact04c.monthlyInterest(100_000, 1500));
        // 1234.56 * 15.00% / 1200 = 15.432 -> 15.43 (COMPUTE without ROUNDED)
        assertEquals(1543, Cbact04c.monthlyInterest(123_456, 1500));
        // 0.99 * 1.00% / 1200 = 0.000825 -> 0.00
        assertEquals(0, Cbact04c.monthlyInterest(99, 100));
        // negative balances truncate toward zero, not toward -infinity
        assertEquals(-1543, Cbact04c.monthlyInterest(-123_456, 1500));
        // 1007.10 * 12.50% / 1200 = 10.490625 -> 10.49
        assertEquals(1049, Cbact04c.monthlyInterest(100_710, 1250));
    }

    @Test
    void monthlyInterestKeepsOnlyElevenDigits() {
        // 12,345,678,901.20 * 1200.00% / 1200 = 12,345,678,901.20 overflows S9(09)V99
        // -> high-order digits dropped, leaving 345,678,901.20
        assertEquals(34_567_890_120L, Cbact04c.monthlyInterest(1_234_567_890_120L, 120_000));
    }

    @Test
    void parmDateIsTenCharacters() {
        assertEquals("2022071800", Cbact04c.parmDate("2022071800"));
        assertEquals("2022071800", Cbact04c.parmDate("20220718001"));
        assertEquals("20220718  ", Cbact04c.parmDate("20220718"));
        assertEquals("          ", Cbact04c.parmDate(null));
    }
}
