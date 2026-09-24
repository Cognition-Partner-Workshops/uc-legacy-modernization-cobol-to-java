package carddemo.batch.programs;

import carddemo.batch.Abend;
import carddemo.batch.BatchContext;
import carddemo.batch.BatchProgram;
import carddemo.batch.io.FixedRecord;
import carddemo.batch.io.KeyedFile;
import carddemo.batch.io.SequentialFile;
import carddemo.batch.records.AccountRecord;
import carddemo.batch.records.CardXrefRecord;
import carddemo.batch.records.TranCatBalRecord;
import carddemo.batch.records.TranRecord;

import java.io.IOException;
import java.util.Optional;

/**
 * CBTRN02C: posts the daily transaction file. Each transaction is validated (card, account,
 * credit limit, expiry); valid ones update the category balance and account and are written to
 * TRANFILE, rejected ones go to DALYREJS with a reason trailer. RETURN-CODE is 4 if anything
 * was rejected.
 */
public final class Cbtrn02c implements BatchProgram {
    static final int REJECT_RECORD_LENGTH = 430;
    static final int RETURN_CODE_REJECTS = 4;
    static final long TEMP_BAL_LIMIT = 100_000_000_000L; // PIC S9(09)V99

    /** WS-VALIDATION-FAIL-REASON / -DESC. */
    public record Rejection(int reason, String description) {
        public static final Rejection NONE = new Rejection(0, "");
        public static final Rejection INVALID_CARD = new Rejection(100, "INVALID CARD NUMBER FOUND");
        public static final Rejection ACCOUNT_NOT_FOUND = new Rejection(101, "ACCOUNT RECORD NOT FOUND");
        public static final Rejection OVERLIMIT = new Rejection(102, "OVERLIMIT TRANSACTION");
        public static final Rejection EXPIRED = new Rejection(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");

        public boolean isRejected() {
            return reason != 0;
        }

        /** The 80-byte VALIDATION-TRAILER: PIC 9(04) reason followed by PIC X(76) description. */
        String trailer() {
            return String.format("%04d%-76s", reason, description);
        }
    }

    private final TranRecord dalyTran = new TranRecord();
    private final TranRecord tran = new TranRecord();
    private final CardXrefRecord xref = new CardXrefRecord();
    private final AccountRecord account = new AccountRecord();
    private final TranCatBalRecord tranCatBal = new TranCatBalRecord();
    private final FixedRecord rejectRecord = new FixedRecord(REJECT_RECORD_LENGTH);

    private long transactionCount;
    private long rejectCount;

    private KeyedFile tranFile;
    private KeyedFile xrefFile;
    private KeyedFile acctFile;
    private KeyedFile tcatbalFile;
    private SequentialFile.Writer rejectsFile;

    /**
     * 1500-B account checks, applied in program order: the expiry check runs even when the
     * limit check already failed, so reason 103 replaces 102.
     */
    static Rejection validateAccount(AccountRecord account, long tranAmt, String origTs) {
        Rejection rejection = Rejection.NONE;
        long tempBal = (account.currCycCredit() - account.currCycDebit() + tranAmt) % TEMP_BAL_LIMIT;
        if (account.creditLimit() < tempBal) {
            rejection = Rejection.OVERLIMIT;
        }
        if (account.expirationDate().compareTo(origTs.substring(0, 10)) < 0) {
            rejection = Rejection.EXPIRED;
        }
        return rejection;
    }

    /** 2800-UPDATE-ACCOUNT-REC arithmetic. */
    static void applyToAccount(AccountRecord account, long tranAmt) {
        account.setCurrBal(account.currBal() + tranAmt);
        if (tranAmt >= 0) {
            account.setCurrCycCredit(account.currCycCredit() + tranAmt);
        } else {
            account.setCurrCycDebit(account.currCycDebit() + tranAmt);
        }
    }

    @Override
    public int run(BatchContext ctx) throws IOException {
        ctx.display("START OF EXECUTION OF PROGRAM CBTRN02C");
        SequentialFile.Reader dalyTranFile = SequentialFile.openInput(ctx.dd("DALYTRAN"), TranRecord.LENGTH);
        tranFile = KeyedFile.create(ctx.dd("TRANFILE"), TranRecord.LENGTH, TranRecord.ID_OFFSET, TranRecord.ID_LENGTH);
        xrefFile = KeyedFile.open(ctx.dd("XREFFILE"), CardXrefRecord.LENGTH,
                CardXrefRecord.CARD_NUM_OFFSET, CardXrefRecord.CARD_NUM_LENGTH);
        rejectsFile = SequentialFile.openOutput(ctx.dd("DALYREJS"), false);
        acctFile = KeyedFile.open(ctx.dd("ACCTFILE"), AccountRecord.LENGTH,
                AccountRecord.ID_OFFSET, AccountRecord.ID_LENGTH);
        tcatbalFile = KeyedFile.open(ctx.dd("TCATBALF"), TranCatBalRecord.LENGTH,
                TranCatBalRecord.KEY_OFFSET, TranCatBalRecord.KEY_LENGTH);
        try {
            Optional<byte[]> rec;
            while ((rec = dalyTranFile.read()).isPresent()) {
                dalyTran.load(rec.get());
                transactionCount++;
                Rejection rejection = validate();
                if (!rejection.isRejected()) {
                    postTransaction(ctx);
                } else {
                    rejectCount++;
                    writeReject(rejection);
                }
            }
        } finally {
            tranFile.close();
            rejectsFile.close();
            acctFile.close();
            tcatbalFile.close();
        }
        ctx.display("TRANSACTIONS PROCESSED :" + String.format("%09d", transactionCount));
        ctx.display("TRANSACTIONS REJECTED  :" + String.format("%09d", rejectCount));
        int returnCode = rejectCount > 0 ? RETURN_CODE_REJECTS : 0;
        ctx.display("END OF EXECUTION OF PROGRAM CBTRN02C");
        return returnCode;
    }

    private Rejection validate() {
        Optional<byte[]> xrefRec = xrefFile.read(dalyTran.cardNum());
        if (xrefRec.isEmpty()) {
            return Rejection.INVALID_CARD;
        }
        xref.load(xrefRec.get());
        Optional<byte[]> acctRec = acctFile.read(xref.acctId());
        if (acctRec.isEmpty()) {
            return Rejection.ACCOUNT_NOT_FOUND;
        }
        account.load(acctRec.get());
        return validateAccount(account, dalyTran.amt(), dalyTran.origTs());
    }

    private void postTransaction(BatchContext ctx) {
        tran.setId(dalyTran.id());
        tran.setTypeCd(dalyTran.typeCd());
        tran.setCatCd(dalyTran.catCd());
        tran.setSource(dalyTran.source());
        tran.setDesc(dalyTran.desc());
        tran.setAmt(dalyTran.amt());
        tran.setMerchantId(dalyTran.merchantId());
        tran.setMerchantName(dalyTran.merchantName());
        tran.setMerchantCity(dalyTran.merchantCity());
        tran.setMerchantZip(dalyTran.merchantZip());
        tran.setCardNum(dalyTran.cardNum());
        tran.setOrigTs(dalyTran.origTs());
        tran.setProcTs(ctx.db2Timestamp());
        updateTranCatBal(ctx);
        applyToAccount(account, dalyTran.amt());
        acctFile.rewrite(account.bytes());
        if (!tranFile.write(tran.bytes())) {
            ctx.display("ERROR WRITING TO TRANSACTION FILE");
            BatchProgram.displayIoStatus(ctx, "22");
            abend(ctx);
        }
    }

    private void updateTranCatBal(BatchContext ctx) {
        String key = TranCatBalRecord.key(xref.acctId(), dalyTran.typeCd(), dalyTran.catCd());
        Optional<byte[]> rec = tcatbalFile.read(key);
        if (rec.isEmpty()) {
            ctx.display("TCATBAL record not found for key : " + key + ".. Creating.");
            tranCatBal.initialize();
            tranCatBal.setAcctId(xref.acctId());
            tranCatBal.setTypeCd(dalyTran.typeCd());
            tranCatBal.setCatCd(dalyTran.catCd());
            tranCatBal.setBal(tranCatBal.bal() + dalyTran.amt());
            if (!tcatbalFile.write(tranCatBal.bytes())) {
                ctx.display("ERROR WRITING TRANSACTION BALANCE FILE");
                BatchProgram.displayIoStatus(ctx, "22");
                abend(ctx);
            }
        } else {
            tranCatBal.load(rec.get());
            tranCatBal.setBal(tranCatBal.bal() + dalyTran.amt());
            tcatbalFile.rewrite(tranCatBal.bytes());
        }
    }

    private void writeReject(Rejection rejection) throws IOException {
        rejectRecord.setBytes(0, dalyTran.bytes());
        rejectRecord.setStr(TranRecord.LENGTH, 80, rejection.trailer());
        rejectsFile.write(rejectRecord.bytes());
    }

    private static void abend(BatchContext ctx) {
        ctx.display("ABENDING PROGRAM");
        throw new Abend(999);
    }
}
