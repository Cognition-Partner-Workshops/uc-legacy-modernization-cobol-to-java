package com.carddemo.common.io;

import com.carddemo.common.model.CustomerRecord;

import static com.carddemo.common.io.FixedWidthParser.*;

/**
 * Parser and formatter for {@link CustomerRecord} matching CVCUS01Y copybook layout.
 * Record length: 500 bytes.
 *
 * <pre>
 * Offset  Len  Field
 * 0        9   CUST-ID                  PIC 9(09)
 * 9       25   CUST-FIRST-NAME          PIC X(25)
 * 34      25   CUST-MIDDLE-NAME         PIC X(25)
 * 59      25   CUST-LAST-NAME           PIC X(25)
 * 84      50   CUST-ADDR-LINE-1         PIC X(50)
 * 134     50   CUST-ADDR-LINE-2         PIC X(50)
 * 184     50   CUST-ADDR-LINE-3         PIC X(50)
 * 234      2   CUST-ADDR-STATE-CD       PIC X(02)
 * 236      3   CUST-ADDR-COUNTRY-CD     PIC X(03)
 * 239     10   CUST-ADDR-ZIP            PIC X(10)
 * 249     15   CUST-PHONE-NUM-1         PIC X(15)
 * 264     15   CUST-PHONE-NUM-2         PIC X(15)
 * 279      9   CUST-SSN                 PIC 9(09)
 * 288     20   CUST-GOVT-ISSUED-ID      PIC X(20)
 * 308     10   CUST-DOB-YYYY-MM-DD      PIC X(10)
 * 318     10   CUST-EFT-ACCOUNT-ID      PIC X(10)
 * 328      1   CUST-PRI-CARD-HOLDER-IND PIC X(01)
 * 329      3   CUST-FICO-CREDIT-SCORE   PIC 9(03)
 * 332    168   FILLER                   PIC X(168)
 * </pre>
 */
public final class CustomerRecordIO {

    private CustomerRecordIO() {}

    public static final RecordParser<CustomerRecord> PARSER = line -> {
        int pos = 0;
        String custId = parseNumericString(line, pos, 9);   pos += 9;
        String firstName = parseAlpha(line, pos, 25);       pos += 25;
        String middleName = parseAlpha(line, pos, 25);      pos += 25;
        String lastName = parseAlpha(line, pos, 25);        pos += 25;
        String addr1 = parseAlpha(line, pos, 50);           pos += 50;
        String addr2 = parseAlpha(line, pos, 50);           pos += 50;
        String addr3 = parseAlpha(line, pos, 50);           pos += 50;
        String stateCd = parseAlpha(line, pos, 2);          pos += 2;
        String countryCd = parseAlpha(line, pos, 3);        pos += 3;
        String zip = parseAlpha(line, pos, 10);             pos += 10;
        String phone1 = parseAlpha(line, pos, 15);          pos += 15;
        String phone2 = parseAlpha(line, pos, 15);          pos += 15;
        String ssn = parseNumericString(line, pos, 9);      pos += 9;
        String govtId = parseAlpha(line, pos, 20);          pos += 20;
        String dob = parseAlpha(line, pos, 10);             pos += 10;
        String eftAcct = parseAlpha(line, pos, 10);         pos += 10;
        String priCard = parseAlpha(line, pos, 1);          pos += 1;
        String fico = parseNumericString(line, pos, 3);

        return new CustomerRecord(custId, firstName, middleName, lastName,
                addr1, addr2, addr3, stateCd, countryCd, zip,
                phone1, phone2, ssn, govtId, dob, eftAcct, priCard, fico);
    };

    public static final RecordFormatter<CustomerRecord> FORMATTER = record -> {
        StringBuilder sb = new StringBuilder(CustomerRecord.RECORD_LENGTH);
        sb.append(formatNumeric(record.custId(), 9));
        sb.append(formatAlpha(record.firstName(), 25));
        sb.append(formatAlpha(record.middleName(), 25));
        sb.append(formatAlpha(record.lastName(), 25));
        sb.append(formatAlpha(record.addrLine1(), 50));
        sb.append(formatAlpha(record.addrLine2(), 50));
        sb.append(formatAlpha(record.addrLine3(), 50));
        sb.append(formatAlpha(record.addrStateCd(), 2));
        sb.append(formatAlpha(record.addrCountryCd(), 3));
        sb.append(formatAlpha(record.addrZip(), 10));
        sb.append(formatAlpha(record.phoneNum1(), 15));
        sb.append(formatAlpha(record.phoneNum2(), 15));
        sb.append(formatNumeric(record.ssn(), 9));
        sb.append(formatAlpha(record.govtIssuedId(), 20));
        sb.append(formatAlpha(record.dobYyyyMmDd(), 10));
        sb.append(formatAlpha(record.eftAccountId(), 10));
        sb.append(formatAlpha(record.priCardHolderInd(), 1));
        sb.append(formatNumeric(record.ficoCreditScore(), 3));
        sb.append(filler(168));
        return sb.toString();
    };
}
