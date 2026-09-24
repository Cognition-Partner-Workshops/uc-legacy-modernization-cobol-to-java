package carddemo.programs;

import carddemo.JobContext;
import carddemo.cobol.Bytes;
import carddemo.cobol.FixedFile;
import carddemo.cobol.KeyedFile;
import carddemo.cobol.RecordArea;
import carddemo.cobol.ZonedDecimal;
import carddemo.records.AccountRecord;
import carddemo.records.CardXrefRecord;
import carddemo.records.TranCatBalRecord;
import carddemo.records.TranRecord;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * CBTRN02C - daily transaction posting. Each daily transaction is validated (card cross
 * reference, account, credit limit, expiration); accepted transactions update the category
 * balance and the account and are written to TRANFILE, rejected ones go to DALYREJS with an
 * 80-byte validation trailer. RETURN-CODE is 4 when anything was rejected.
 */
public final class Cbtrn02c implements BatchProgram {
    public static final int RC_REJECTS = 4;

    /** Validation outcome; {@code reason == 0} means the transaction is accepted. */
    public record Validation(int reason, String description) {
        public static final Validation OK = new Validation(0, "");
        public static final Validation INVALID_CARD = new Validation(100, "INVALID CARD NUMBER FOUND");
        public static final Validation ACCOUNT_NOT_FOUND = new Validation(101, "ACCOUNT RECORD NOT FOUND");
        public static final Validation OVERLIMIT = new Validation(102, "OVERLIMIT TRANSACTION");
        public static final Validation EXPIRED =
                new Validation(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");

        public boolean accepted() {
            return reason == 0;
        }
    }

    /** REJECT-RECORD: the 350-byte daily transaction followed by the 80-byte WS-VALIDATION-TRAILER. */
    public static final class RejectRecord extends RecordArea {
        public static final int LENGTH = TranRecord.LENGTH + 80;
        public static final Alnum TRAN_DATA = new Alnum(0, TranRecord.LENGTH);
        public static final Num FAIL_REASON = new Num(TranRecord.LENGTH, 4, 0, false);
        public static final Alnum FAIL_REASON_DESC = new Alnum(TranRecord.LENGTH + 4, 76);

        public RejectRecord() {
            super(LENGTH);
        }
    }

    /** Files the posting step works against; kept in memory and saved back in key order. */
    private record Files(KeyedFile xref, KeyedFile accounts, KeyedFile tcatbal) {
    }

    /**
     * {@code 1500-B-LOOKUP-ACCT} account-level checks. Both checks run and the later one wins,
     * so an over-limit transaction on an expired account is reported as expired (reason 103).
     */
    public static Validation validateAccount(AccountRecord acct, TranRecord daily) {
        BigDecimal tempBal = acct.get(AccountRecord.CURR_CYC_CREDIT)
                .subtract(acct.get(AccountRecord.CURR_CYC_DEBIT))
                .add(daily.get(TranRecord.AMT));
        Validation result = Validation.OK;
        if (acct.get(AccountRecord.CREDIT_LIMIT).compareTo(tempBal) < 0) {
            result = Validation.OVERLIMIT;
        }
        byte[] expiration = acct.get(AccountRecord.EXPIRATION_DATE);
        byte[] origDate = Bytes.slice(daily.get(TranRecord.ORIG_TS), 0, 10);
        if (Bytes.compare(expiration, origDate) < 0) {
            result = Validation.EXPIRED;
        }
        return result;
    }

    /** {@code 2800-UPDATE-ACCOUNT-REC}: balance moves by the amount; credits and debits are tracked separately. */
    public static void postToAccount(AccountRecord acct, BigDecimal amount) {
        acct.set(AccountRecord.CURR_BAL, acct.get(AccountRecord.CURR_BAL).add(amount));
        if (amount.signum() >= 0) {
            acct.set(AccountRecord.CURR_CYC_CREDIT, acct.get(AccountRecord.CURR_CYC_CREDIT).add(amount));
        } else {
            acct.set(AccountRecord.CURR_CYC_DEBIT, acct.get(AccountRecord.CURR_CYC_DEBIT).add(amount));
        }
    }

    /** {@code 2500-WRITE-REJECT-REC} image. */
    public static byte[] rejectRecord(TranRecord daily, Validation validation) {
        RejectRecord reject = new RejectRecord();
        reject.set(RejectRecord.TRAN_DATA, daily.bytes());
        reject.set(RejectRecord.FAIL_REASON, validation.reason());
        reject.set(RejectRecord.FAIL_REASON_DESC, validation.description());
        return reject.bytes();
    }

    /** {@code 2000-POST-TRANSACTION} field moves: the posted transaction is the daily one stamped with the processing time. */
    public static void buildPostedTransaction(TranRecord tran, TranRecord daily, String timestamp) {
        tran.set(TranRecord.ID, daily.get(TranRecord.ID));
        tran.set(TranRecord.TYPE_CD, daily.get(TranRecord.TYPE_CD));
        tran.set(TranRecord.CAT_CD, daily.get(TranRecord.CAT_CD));
        tran.set(TranRecord.SOURCE, daily.get(TranRecord.SOURCE));
        tran.set(TranRecord.DESC, daily.get(TranRecord.DESC));
        tran.set(TranRecord.AMT, daily.get(TranRecord.AMT));
        tran.set(TranRecord.MERCHANT_ID, daily.get(TranRecord.MERCHANT_ID));
        tran.set(TranRecord.MERCHANT_NAME, daily.get(TranRecord.MERCHANT_NAME));
        tran.set(TranRecord.MERCHANT_CITY, daily.get(TranRecord.MERCHANT_CITY));
        tran.set(TranRecord.MERCHANT_ZIP, daily.get(TranRecord.MERCHANT_ZIP));
        tran.set(TranRecord.CARD_NUM, daily.get(TranRecord.CARD_NUM));
        tran.set(TranRecord.ORIG_TS, daily.get(TranRecord.ORIG_TS));
        tran.set(TranRecord.PROC_TS, timestamp);
    }

    @Override
    public int run(JobContext job) {
        job.display("START OF EXECUTION OF PROGRAM CBTRN02C");
        Files files = new Files(
                KeyedFile.load(job.dd("XREFFILE"), CardXrefRecord.LENGTH,
                        CardXrefRecord.KEY_OFFSET, CardXrefRecord.KEY_LENGTH),
                KeyedFile.load(job.dd("ACCTFILE"), AccountRecord.LENGTH,
                        AccountRecord.KEY_OFFSET, AccountRecord.KEY_LENGTH),
                KeyedFile.load(job.dd("TCATBALF"), TranCatBalRecord.LENGTH,
                        TranCatBalRecord.KEY_OFFSET, TranCatBalRecord.KEY_LENGTH));
        KeyedFile tranFile = new KeyedFile(TranRecord.LENGTH, TranRecord.KEY_OFFSET, TranRecord.KEY_LENGTH);

        TranRecord daily = new TranRecord();
        TranRecord tran = new TranRecord();
        CardXrefRecord xref = new CardXrefRecord();
        AccountRecord acct = new AccountRecord();
        TranCatBalRecord catBal = new TranCatBalRecord();

        int transactionCount = 0;
        int rejectCount = 0;
        try (FixedFile.Writer rejects = new FixedFile.Writer(job.dd("DALYREJS"))) {
            for (byte[] image : FixedFile.readAll(job.dd("DALYTRAN"), TranRecord.LENGTH)) {
                daily.load(image);
                transactionCount++;
                Validation validation = validate(files, daily, xref, acct);
                if (validation.accepted()) {
                    buildPostedTransaction(tran, daily, job.timestamp());
                    updateCategoryBalance(job, files.tcatbal(), catBal, xref, daily);
                    postToAccount(acct, daily.get(TranRecord.AMT));
                    files.accounts().rewrite(acct.bytes());
                    writeTransaction(job, tranFile, tran.bytes());
                } else {
                    rejectCount++;
                    rejects.write(rejectRecord(daily, validation));
                }
            }
        }
        tranFile.save(job.dd("TRANFILE"));
        files.accounts().save(job.dd("ACCTFILE"));
        files.tcatbal().save(job.dd("TCATBALF"));

        job.display("TRANSACTIONS PROCESSED :" + ZonedDecimal.displayText(BigDecimal.valueOf(transactionCount), 9, 0, false));
        job.display("TRANSACTIONS REJECTED  :" + ZonedDecimal.displayText(BigDecimal.valueOf(rejectCount), 9, 0, false));
        job.display("END OF EXECUTION OF PROGRAM CBTRN02C");
        return rejectCount > 0 ? RC_REJECTS : 0;
    }

    /** {@code 1500-VALIDATE-TRAN}: XREF lookup first, account checks only when the card is known. */
    private static Validation validate(Files files, TranRecord daily, CardXrefRecord xref, AccountRecord acct) {
        Optional<byte[]> xrefImage = files.xref().read(daily.get(TranRecord.CARD_NUM));
        if (xrefImage.isEmpty()) {
            return Validation.INVALID_CARD;
        }
        xref.load(xrefImage.get());
        Optional<byte[]> acctImage = files.accounts().read(xref.get(CardXrefRecord.ACCT_ID_TEXT));
        if (acctImage.isEmpty()) {
            return Validation.ACCOUNT_NOT_FOUND;
        }
        acct.load(acctImage.get());
        return validateAccount(acct, daily);
    }

    /** {@code 2700-UPDATE-TCATBAL}: add the amount to the category balance, creating the record when absent. */
    private static void updateCategoryBalance(JobContext job, KeyedFile tcatbal, TranCatBalRecord catBal,
            CardXrefRecord xref, TranRecord daily) {
        byte[] key = new byte[TranCatBalRecord.KEY_LENGTH];
        Bytes.moveAlnum(xref.get(CardXrefRecord.ACCT_ID_TEXT), key, TranCatBalRecord.ACCT_ID_TEXT.offset(),
                TranCatBalRecord.ACCT_ID_TEXT.length());
        Bytes.moveAlnum(daily.get(TranRecord.TYPE_CD), key, TranCatBalRecord.TYPE_CD.offset(),
                TranCatBalRecord.TYPE_CD.length());
        Bytes.moveAlnum(daily.get(TranRecord.CAT_CD_TEXT), key, TranCatBalRecord.CAT_CD_TEXT.offset(),
                TranCatBalRecord.CAT_CD_TEXT.length());
        BigDecimal amount = daily.get(TranRecord.AMT);

        Optional<byte[]> existing = tcatbal.read(key);
        if (existing.isPresent()) {
            catBal.load(existing.get());
            catBal.set(TranCatBalRecord.BAL, catBal.get(TranCatBalRecord.BAL).add(amount));
            tcatbal.rewrite(catBal.bytes());
        } else {
            job.display(Bytes.ascii("TCATBAL record not found for key : "), key, Bytes.ascii(".. Creating."));
            catBal.initialize();
            catBal.set(TranCatBalRecord.ACCT_ID, xref.get(CardXrefRecord.ACCT_ID));
            catBal.set(TranCatBalRecord.TYPE_CD, daily.get(TranRecord.TYPE_CD));
            catBal.set(TranCatBalRecord.CAT_CD, daily.get(TranRecord.CAT_CD));
            catBal.set(TranCatBalRecord.BAL, amount);
            tcatbal.write(catBal.bytes());
        }
    }

    private static void writeTransaction(JobContext job, KeyedFile tranFile, byte[] image) {
        if (tranFile.contains(tranFile.keyOf(image))) {
            throw Abend.ioError(job, "ERROR WRITING TO TRANSACTION FILE", "22");
        }
        tranFile.write(image);
    }
}
