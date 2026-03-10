package com.carddemo.common.io;

import com.carddemo.common.model.AccountRecord;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRecordIOTest {

    @Test
    void roundTrip_parseAndFormat() {
        AccountRecord original = new AccountRecord(
                "00000000001",          // acctId
                "Y",                    // activeStatus
                new BigDecimal("1000.00"),    // currBal
                new BigDecimal("5000.00"),    // creditLimit
                new BigDecimal("2000.00"),    // cashCreditLimit
                "2020-01-01",           // openDate
                "2025-12-31",           // expirationDate
                "2023-06-15",           // reissueDate
                new BigDecimal("500.00"),     // currCycCredit
                new BigDecimal("200.00"),     // currCycDebit
                "10001",                // addrZip
                "GROUP1"                // groupId
        );

        String formatted = AccountRecordIO.FORMATTER.format(original);
        assertThat(formatted.length()).isEqualTo(AccountRecord.RECORD_LENGTH);

        AccountRecord parsed = AccountRecordIO.PARSER.parse(formatted);
        assertThat(parsed.acctId()).isEqualTo("00000000001");
        assertThat(parsed.activeStatus()).isEqualTo("Y");
        assertThat(parsed.currBal()).isEqualByComparingTo("1000.00");
        assertThat(parsed.creditLimit()).isEqualByComparingTo("5000.00");
        assertThat(parsed.cashCreditLimit()).isEqualByComparingTo("2000.00");
        assertThat(parsed.openDate()).isEqualTo("2020-01-01");
        assertThat(parsed.expirationDate()).isEqualTo("2025-12-31");
        assertThat(parsed.reissueDate()).isEqualTo("2023-06-15");
        assertThat(parsed.currCycCredit()).isEqualByComparingTo("500.00");
        assertThat(parsed.currCycDebit()).isEqualByComparingTo("200.00");
        assertThat(parsed.addrZip()).isEqualTo("10001");
        assertThat(parsed.groupId()).isEqualTo("GROUP1");
    }

    @Test
    void formatter_producesCorrectLength() {
        AccountRecord record = new AccountRecord(
                "12345678901", "N",
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "2020-01-01", "2025-12-31", "2023-06-15",
                BigDecimal.ZERO, BigDecimal.ZERO, "00000", "GRP01"
        );
        String formatted = AccountRecordIO.FORMATTER.format(record);
        assertThat(formatted.length()).isEqualTo(300);
    }
}
