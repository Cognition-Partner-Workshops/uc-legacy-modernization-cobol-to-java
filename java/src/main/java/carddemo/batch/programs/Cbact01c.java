package carddemo.batch.programs;

import carddemo.batch.BatchContext;
import carddemo.batch.BatchProgram;
import carddemo.batch.io.FixedRecord;
import carddemo.batch.io.SequentialFile;
import carddemo.batch.io.Zoned;
import carddemo.batch.records.AccountRecord;

import java.io.IOException;
import java.util.Optional;

/**
 * CBACT01C: sequentially prints the account master and writes three derived files —
 * OUTFILE (107-byte record with a COMP-3 cycle debit), ARRYFILE (110-byte record with a
 * five-element balance array) and VBRCFILE (variable records of 12 and 39 bytes).
 */
public final class Cbact01c implements BatchProgram {
    static final int OUT_RECORD_LENGTH = 107;
    static final int ARRAY_RECORD_LENGTH = 110;
    static final long DEFAULT_CYC_DEBIT_CENTS = 2525_00L;

    private final AccountRecord account = new AccountRecord();
    private final OutAcctRecord outRec = new OutAcctRecord();
    private final ArrayRecord arrRec = new ArrayRecord();
    private final VbrcRec1 vb1 = new VbrcRec1();
    private final VbrcRec2 vb2 = new VbrcRec2();

    @Override
    public int run(BatchContext ctx) throws IOException {
        ctx.display("START OF EXECUTION OF PROGRAM CBACT01C");
        SequentialFile.Reader acctFile = SequentialFile.openInput(ctx.dd("ACCTFILE"), AccountRecord.LENGTH);
        SequentialFile.Writer outFile = SequentialFile.openOutput(ctx.dd("OUTFILE"), false);
        SequentialFile.Writer arrFile = SequentialFile.openOutput(ctx.dd("ARRYFILE"), false);
        SequentialFile.Writer vbrFile = SequentialFile.openOutput(ctx.dd("VBRCFILE"), true);
        try {
            Optional<byte[]> rec;
            while ((rec = acctFile.read()).isPresent()) {
                account.load(rec.get());
                arrRec.initialize();
                displayAccount(ctx);
                populateOutRecord();
                outFile.write(outRec.bytes());
                populateArrayRecord();
                arrFile.write(arrRec.bytes());
                populateVbrcRecords(ctx);
                vbrFile.write(vb1.bytes());
                vbrFile.write(vb2.bytes());
                ctx.display(account.text());
            }
        } finally {
            outFile.close();
            arrFile.close();
            vbrFile.close();
        }
        ctx.display("END OF EXECUTION OF PROGRAM CBACT01C");
        return 0;
    }

    private void displayAccount(BatchContext ctx) {
        ctx.display("ACCT-ID                 :" + account.id());
        ctx.display("ACCT-ACTIVE-STATUS      :" + account.activeStatus());
        ctx.display("ACCT-CURR-BAL           :" + Zoned.display(account.currBal(), AccountRecord.AMOUNT_LENGTH));
        ctx.display("ACCT-CREDIT-LIMIT       :" + Zoned.display(account.creditLimit(), AccountRecord.AMOUNT_LENGTH));
        ctx.display("ACCT-CASH-CREDIT-LIMIT  :"
                + Zoned.display(account.cashCreditLimit(), AccountRecord.AMOUNT_LENGTH));
        ctx.display("ACCT-OPEN-DATE          :" + account.openDate());
        ctx.display("ACCT-EXPIRAION-DATE     :" + account.expirationDate());
        ctx.display("ACCT-REISSUE-DATE       :" + account.reissueDate());
        ctx.display("ACCT-CURR-CYC-CREDIT    :" + Zoned.display(account.currCycCredit(), AccountRecord.AMOUNT_LENGTH));
        ctx.display("ACCT-CURR-CYC-DEBIT     :" + Zoned.display(account.currCycDebit(), AccountRecord.AMOUNT_LENGTH));
        ctx.display("ACCT-GROUP-ID           :" + account.groupId());
        ctx.display("-------------------------------------------------");
    }

    private void populateOutRecord() {
        outRec.setStr(0, 11, account.id());
        outRec.setStr(11, 1, account.activeStatus());
        outRec.setBytes(12, account.currBalBytes());
        outRec.setBytes(24, account.creditLimitBytes());
        outRec.setBytes(36, account.cashCreditLimitBytes());
        outRec.setStr(48, 10, account.openDate());
        outRec.setStr(58, 10, account.expirationDate());
        outRec.setStr(68, 10, compactDate(account.reissueDate()));
        outRec.setBytes(78, account.currCycCreditBytes());
        if (account.currCycDebit() == 0) {
            outRec.setPacked(90, 12, DEFAULT_CYC_DEBIT_CENTS);
        }
        outRec.setStr(97, 10, account.groupId());
    }

    /** COBDATFT type 2 -> 2: 'YYYY-MM-DD' becomes 'YYYYMMDD' in a 20-byte space-padded area. */
    static String compactDate(String isoDate) {
        return isoDate.substring(0, 4) + isoDate.substring(5, 7) + isoDate.substring(8, 10);
    }

    private void populateArrayRecord() {
        arrRec.setStr(0, 11, account.id());
        arrRec.setCurrBal(1, account.currBalBytes());
        arrRec.setCycDebit(1, 1005_00L);
        arrRec.setCurrBal(2, account.currBalBytes());
        arrRec.setCycDebit(2, 1525_00L);
        arrRec.setCurrBalValue(3, -1025_00L);
        arrRec.setCycDebit(3, -2500_00L);
    }

    private void populateVbrcRecords(BatchContext ctx) {
        vb1.setStr(0, 11, account.id());
        vb1.setStr(11, 1, account.activeStatus());
        vb2.setStr(0, 11, account.id());
        vb2.setBytes(11, account.currBalBytes());
        vb2.setBytes(23, account.creditLimitBytes());
        vb2.setStr(35, 4, account.reissueDate().substring(0, 4));
        ctx.display("VBRC-REC1:" + vb1.text());
        ctx.display("VBRC-REC2:" + vb2.text());
    }

    /** OUT-ACCT-REC: like the account header but with a packed cycle debit and no filler. */
    static final class OutAcctRecord extends FixedRecord {
        OutAcctRecord() {
            super(OUT_RECORD_LENGTH);
            setPacked(90, 12, 0);
        }



    }

    /** ARR-ARRAY-REC: account id, 5 x (zoned balance, packed cycle debit), 4-byte filler. */
    static final class ArrayRecord extends FixedRecord {
        private static final int ELEMENT = 19;

        ArrayRecord() {
            super(ARRAY_RECORD_LENGTH);
        }

        /** INITIALIZE: zoned fields to '0's, packed fields to +0; FILLER untouched. */
        void initialize() {
            setUnsigned(0, 11, 0);
            for (int i = 1; i <= 5; i++) {
                setUnsigned(balOffset(i), 12, 0);
                setPacked(debitOffset(i), 12, 0);
            }
        }

        private static int balOffset(int i) {
            return 11 + (i - 1) * ELEMENT;
        }

        private static int debitOffset(int i) {
            return balOffset(i) + 12;
        }


        void setCurrBal(int i, byte[] zoned) {
            setBytes(balOffset(i), zoned);
        }

        void setCurrBalValue(int i, long cents) {
            setSigned(balOffset(i), 12, cents);
        }

        void setCycDebit(int i, long cents) {
            setPacked(debitOffset(i), 12, cents);
        }
    }

    static final class VbrcRec1 extends FixedRecord {
        VbrcRec1() {
            super(12);
        }

    }

    static final class VbrcRec2 extends FixedRecord {
        VbrcRec2() {
            super(39);
        }


    }
}
