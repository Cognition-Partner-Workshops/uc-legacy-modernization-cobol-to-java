package com.carddemo.common.io;

import com.carddemo.common.model.AccountRecord;

import static com.carddemo.common.io.FixedWidthParser.*;

/**
 * Parser and formatter for {@link AccountRecord} matching CVACT01Y copybook layout.
 * Record length: 300 bytes.
 *
 * <pre>
 * Offset  Len  Field
 * 0       11   ACCT-ID              PIC 9(11)
 * 11       1   ACCT-ACTIVE-STATUS   PIC X(01)
 * 12      12   ACCT-CURR-BAL        PIC S9(10)V99  (12 bytes display)
 * 24      12   ACCT-CREDIT-LIMIT    PIC S9(10)V99
 * 36      12   ACCT-CASH-CREDIT-LIM PIC S9(10)V99
 * 48      10   ACCT-OPEN-DATE       PIC X(10)
 * 58      10   ACCT-EXPIRAION-DATE  PIC X(10)
 * 68      10   ACCT-REISSUE-DATE    PIC X(10)
 * 78      12   ACCT-CURR-CYC-CREDIT PIC S9(10)V99
 * 90      12   ACCT-CURR-CYC-DEBIT  PIC S9(10)V99
 * 102     10   ACCT-ADDR-ZIP        PIC X(10)
 * 112     10   ACCT-GROUP-ID        PIC X(10)
 * 122    178   FILLER               PIC X(178)
 * </pre>
 */
public final class AccountRecordIO {

    /** Display width of PIC S9(10)V99: 10 integer + 2 decimal = 12 bytes (sign via overpunch). */
    private static final int SIGNED_DEC_WIDTH = 12;

    private AccountRecordIO() {}

    public static final RecordParser<AccountRecord> PARSER = line -> {
        int pos = 0;
        String acctId = parseNumericString(line, pos, 11);             pos += 11;
        String status = parseAlpha(line, pos, 1);                      pos += 1;
        var currBal = parseSignedDecimal(line, pos, SIGNED_DEC_WIDTH, 2);        pos += SIGNED_DEC_WIDTH;
        var creditLimit = parseSignedDecimal(line, pos, SIGNED_DEC_WIDTH, 2);    pos += SIGNED_DEC_WIDTH;
        var cashCreditLimit = parseSignedDecimal(line, pos, SIGNED_DEC_WIDTH, 2); pos += SIGNED_DEC_WIDTH;
        String openDate = parseAlpha(line, pos, 10);                   pos += 10;
        String expDate = parseAlpha(line, pos, 10);                    pos += 10;
        String reissueDate = parseAlpha(line, pos, 10);                pos += 10;
        var cycCredit = parseSignedDecimal(line, pos, SIGNED_DEC_WIDTH, 2);      pos += SIGNED_DEC_WIDTH;
        var cycDebit = parseSignedDecimal(line, pos, SIGNED_DEC_WIDTH, 2);       pos += SIGNED_DEC_WIDTH;
        String zip = parseAlpha(line, pos, 10);                        pos += 10;
        String groupId = parseAlpha(line, pos, 10);

        return new AccountRecord(acctId, status, currBal, creditLimit,
                cashCreditLimit, openDate, expDate, reissueDate,
                cycCredit, cycDebit, zip, groupId);
    };

    public static final RecordFormatter<AccountRecord> FORMATTER = record -> {
        StringBuilder sb = new StringBuilder(AccountRecord.RECORD_LENGTH);
        sb.append(formatNumeric(record.acctId(), 11));
        sb.append(formatAlpha(record.activeStatus(), 1));
        sb.append(formatSignedDecimalFixed(record.currBal(), SIGNED_DEC_WIDTH, 2));
        sb.append(formatSignedDecimalFixed(record.creditLimit(), SIGNED_DEC_WIDTH, 2));
        sb.append(formatSignedDecimalFixed(record.cashCreditLimit(), SIGNED_DEC_WIDTH, 2));
        sb.append(formatAlpha(record.openDate(), 10));
        sb.append(formatAlpha(record.expirationDate(), 10));
        sb.append(formatAlpha(record.reissueDate(), 10));
        sb.append(formatSignedDecimalFixed(record.currCycCredit(), SIGNED_DEC_WIDTH, 2));
        sb.append(formatSignedDecimalFixed(record.currCycDebit(), SIGNED_DEC_WIDTH, 2));
        sb.append(formatAlpha(record.addrZip(), 10));
        sb.append(formatAlpha(record.groupId(), 10));
        sb.append(filler(178));
        return sb.toString();
    };
}
