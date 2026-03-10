package com.carddemo.common.io;

import com.carddemo.common.model.CustomerRecord;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRecordIOTest {

    @Test
    void roundTrip_parseAndFormat() {
        CustomerRecord original = new CustomerRecord(
                "000000001",       // custId
                "John",            // firstName
                "M",               // middleName
                "Doe",             // lastName
                "123 Main St",     // addrLine1
                "Apt 4",           // addrLine2
                "",                // addrLine3
                "NY",              // addrStateCd
                "US",              // addrCountryCd
                "10001",           // addrZip
                "212-555-1234",    // phoneNum1
                "212-555-5678",    // phoneNum2
                "123456789",       // ssn
                "DL12345",         // govtIssuedId
                "1990-01-15",      // dobYyyyMmDd
                "ACCT00001",       // eftAccountId
                "Y",               // priCardHolderInd
                "750"              // ficoCreditScore
        );

        String formatted = CustomerRecordIO.FORMATTER.format(original);
        assertThat(formatted.length()).isEqualTo(CustomerRecord.RECORD_LENGTH);

        CustomerRecord parsed = CustomerRecordIO.PARSER.parse(formatted);
        assertThat(parsed.custId()).isEqualTo("000000001");
        assertThat(parsed.firstName()).isEqualTo("John");
        assertThat(parsed.lastName()).isEqualTo("Doe");
        assertThat(parsed.addrStateCd()).isEqualTo("NY");
        assertThat(parsed.ssn()).isEqualTo("123456789");
        assertThat(parsed.ficoCreditScore()).isEqualTo("750");
    }

    @Test
    void formatter_producesCorrectLength() {
        CustomerRecord record = new CustomerRecord(
                "000000001", "A", "B", "C", "D", "E", "F",
                "NY", "US", "10001", "1234567890", "0987654321",
                "111223333", "ID1", "2000-01-01", "ACCT1", "Y", "700"
        );
        String formatted = CustomerRecordIO.FORMATTER.format(record);
        assertThat(formatted.length()).isEqualTo(500);
    }
}
