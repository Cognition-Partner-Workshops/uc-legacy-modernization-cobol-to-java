package com.carddemo.batch.job;

import com.carddemo.batch.model.CustomerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerFileProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void executeReadsCustomerRecords() throws IOException {
        Path inputFile = tempDir.resolve("custdata.txt");
        // Build a 332-char record: custId(9) + firstName(25) + middleName(25) + lastName(25)
        // + addr1(50) + addr2(50) + addr3(50) + stateCd(2) + countryCd(3) + zip(10)
        // + phone1(15) + phone2(15) + ssn(9) + govtId(20) + dob(10) + eftAcctId(10)
        // + priCardHolder(1) + fico(3)
        StringBuilder sb = new StringBuilder();
        sb.append("000000001");                           // custId
        sb.append(String.format("%-25s", "John"));        // firstName
        sb.append(String.format("%-25s", "M"));           // middleName
        sb.append(String.format("%-25s", "Doe"));         // lastName
        sb.append(String.format("%-50s", "123 Main St")); // addr1
        sb.append(String.format("%-50s", "Apt 4B"));      // addr2
        sb.append(String.format("%-50s", ""));            // addr3
        sb.append("WA");                                  // stateCd
        sb.append("USA");                                 // countryCd
        sb.append(String.format("%-10s", "98101"));       // zip
        sb.append(String.format("%-15s", "2065551234"));  // phone1
        sb.append(String.format("%-15s", ""));            // phone2
        sb.append("123456789");                           // ssn
        sb.append(String.format("%-20s", "DL12345"));     // govtId
        sb.append("1990-05-15");                          // dob
        sb.append(String.format("%-10s", "EFT001"));      // eftAcctId
        sb.append("Y");                                   // priCardHolder
        sb.append("750");                                 // fico

        Files.writeString(inputFile, sb.toString() + "\n");

        CustomerFileProcessor processor = new CustomerFileProcessor(inputFile);
        List<CustomerRecord> records = processor.execute();

        assertEquals(1, records.size());
        CustomerRecord rec = records.get(0);
        assertEquals(1L, rec.getCustId());
        assertEquals("John", rec.getCustFirstName());
        assertEquals("Doe", rec.getCustLastName());
        assertEquals("WA", rec.getCustAddrStateCd());
        assertEquals("USA", rec.getCustAddrCountryCd());
        assertEquals(750, rec.getCustFicoCreditScore());
    }

    @Test
    void executeHandlesEmptyFile() throws IOException {
        Path inputFile = tempDir.resolve("custdata.txt");
        Files.writeString(inputFile, "");
        CustomerFileProcessor processor = new CustomerFileProcessor(inputFile);
        List<CustomerRecord> records = processor.execute();
        assertTrue(records.isEmpty());
    }

    @Test
    void parseCustomerRecordHandlesShortLine() {
        CustomerRecord rec = CustomerFileProcessor.parseCustomerRecord("000000001John");
        assertEquals(1L, rec.getCustId());
        assertEquals("John", rec.getCustFirstName());
    }
}
