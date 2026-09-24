package carddemo.batch.programs;

import carddemo.batch.Abend;
import carddemo.batch.BatchContext;
import carddemo.batch.BatchProgram;
import carddemo.batch.io.KeyedFile;
import carddemo.batch.io.SequentialFile;
import carddemo.batch.records.AccountRecord;
import carddemo.batch.records.CardXrefRecord;
import carddemo.batch.records.DisGroupRecord;
import carddemo.batch.records.TranCatBalRecord;
import carddemo.batch.records.TranRecord;

import java.io.IOException;
import java.util.Optional;

/**
 * CBACT04C: monthly interest. Walks TCATBALF in key order, looks up the disclosure-group rate
 * (falling back to group DEFAULT), writes one interest transaction per category with a
 * non-zero rate and, whenever the account changes, posts the accumulated interest to the
 * previous account and clears its cycle totals.
 */
public final class Cbact04c implements BatchProgram {
    static final String DEFAULT_GROUP = "DEFAULT";
    static final long MONTHLY_INT_LIMIT = 100_000_000_000L; // PIC S9(09)V99

    private final TranCatBalRecord tranCatBal = new TranCatBalRecord();
    private final CardXrefRecord xref = new CardXrefRecord();
    private final DisGroupRecord disGroup = new DisGroupRecord();
    private final AccountRecord account = new AccountRecord();
    private final TranRecord tran = new TranRecord();

    private String lastAcctNum = "           ";
    private long monthlyInt;
    private long totalInt;
    private boolean firstTime = true;
    private long tranIdSuffix;

    private KeyedFile acctFile;
    private KeyedFile xrefFile;
    private KeyedFile discGrpFile;
    private SequentialFile.Writer tranFile;

    /** COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200, truncated to cents. */
    static long monthlyInterest(long balanceCents, long rateScaled) {
        long cents = balanceCents * rateScaled / 1200_00L;
        return cents % MONTHLY_INT_LIMIT;
    }

    @Override
    public int run(BatchContext ctx) throws IOException {
        ctx.display("START OF EXECUTION OF PROGRAM CBACT04C");
        String parmDate = parmDate(ctx.parm());
        KeyedFile tcatbalFile = KeyedFile.open(ctx.dd("TCATBALF"), TranCatBalRecord.LENGTH,
                TranCatBalRecord.KEY_OFFSET, TranCatBalRecord.KEY_LENGTH);
        xrefFile = KeyedFile.open(ctx.dd("XREFFILE"), CardXrefRecord.LENGTH,
                CardXrefRecord.CARD_NUM_OFFSET, CardXrefRecord.CARD_NUM_LENGTH);
        discGrpFile = KeyedFile.open(ctx.dd("DISCGRP"), DisGroupRecord.LENGTH,
                DisGroupRecord.KEY_OFFSET, DisGroupRecord.KEY_LENGTH);
        acctFile = KeyedFile.open(ctx.dd("ACCTFILE"), AccountRecord.LENGTH,
                AccountRecord.ID_OFFSET, AccountRecord.ID_LENGTH);
        tranFile = SequentialFile.openOutput(ctx.dd("TRANSACT"), false);
        try {
            Optional<byte[]> rec;
            while ((rec = tcatbalFile.readNext()).isPresent()) {
                tranCatBal.load(rec.get());
                ctx.display(tranCatBal.text());
                if (!tranCatBal.acctId().equals(lastAcctNum)) {
                    if (!firstTime) {
                        updateAccount(ctx);
                    } else {
                        firstTime = false;
                    }
                    totalInt = 0;
                    lastAcctNum = tranCatBal.acctId();
                    getAcctData(ctx, lastAcctNum);
                    getXrefData(ctx, lastAcctNum);
                }
                getInterestRate(ctx, account.groupId(), tranCatBal.typeCd(), tranCatBal.catCd());
                if (disGroup.intRate() != 0) {
                    computeInterest(ctx, parmDate);
                }
            }
        } finally {
            acctFile.close();
            tranFile.close();
        }
        ctx.display("END OF EXECUTION OF PROGRAM CBACT04C");
        return 0;
    }

    /** PARM-DATE PIC X(10): the PARM truncated or space padded to 10. */
    static String parmDate(String parm) {
        String p = parm == null ? "" : parm;
        return p.length() >= 10 ? p.substring(0, 10) : String.format("%-10s", p);
    }

    private void updateAccount(BatchContext ctx) {
        account.setCurrBal(account.currBal() + totalInt);
        account.setCurrCycCredit(0);
        account.setCurrCycDebit(0);
        if (!acctFile.rewrite(account.bytes())) {
            ctx.display("ERROR RE-WRITING ACCOUNT FILE");
            BatchProgram.displayIoStatus(ctx, "23");
            abend(ctx);
        }
    }

    private void getAcctData(BatchContext ctx, String acctId) {
        Optional<byte[]> rec = acctFile.read(acctId);
        if (rec.isEmpty()) {
            ctx.display("ACCOUNT NOT FOUND: " + acctId);
            ctx.display("ERROR READING ACCOUNT FILE");
            BatchProgram.displayIoStatus(ctx, "23");
            abend(ctx);
        }
        account.load(rec.get());
    }

    private void getXrefData(BatchContext ctx, String acctId) {
        Optional<byte[]> rec = xrefFile.readByAlternateKey(CardXrefRecord.ACCT_ID_OFFSET, acctId);
        if (rec.isEmpty()) {
            ctx.display("ACCOUNT NOT FOUND: " + acctId);
            ctx.display("ERROR READING XREF FILE");
            BatchProgram.displayIoStatus(ctx, "23");
            abend(ctx);
        }
        xref.load(rec.get());
    }

    private void getInterestRate(BatchContext ctx, String groupId, String typeCd, String catCd) {
        Optional<byte[]> rec = discGrpFile.read(DisGroupRecord.key(groupId, typeCd, catCd));
        if (rec.isPresent()) {
            disGroup.load(rec.get());
            return;
        }
        ctx.display("DISCLOSURE GROUP RECORD MISSING");
        ctx.display("TRY WITH DEFAULT GROUP CODE");
        Optional<byte[]> dflt = discGrpFile.read(DisGroupRecord.key(String.format("%-10s", DEFAULT_GROUP), typeCd, catCd));
        if (dflt.isEmpty()) {
            ctx.display("ERROR READING DEFAULT DISCLOSURE GROUP");
            BatchProgram.displayIoStatus(ctx, "23");
            abend(ctx);
        }
        disGroup.load(dflt.get());
    }

    private void computeInterest(BatchContext ctx, String parmDate) throws IOException {
        monthlyInt = monthlyInterest(tranCatBal.bal(), disGroup.intRate());
        totalInt += monthlyInt;
        writeInterestTransaction(ctx, parmDate);
    }

    private void writeInterestTransaction(BatchContext ctx, String parmDate) throws IOException {
        tranIdSuffix = (tranIdSuffix + 1) % 1_000_000;
        tran.setId(parmDate + String.format("%06d", tranIdSuffix));
        tran.setTypeCd("01");
        tran.setCatCd("0005");
        tran.setSource("System");
        tran.setDesc("Int. for a/c " + account.id());
        tran.setAmt(monthlyInt);
        tran.setMerchantId(0);
        tran.setMerchantName("");
        tran.setMerchantCity("");
        tran.setMerchantZip("");
        tran.setCardNum(xref.cardNum());
        String ts = ctx.db2Timestamp();
        tran.setOrigTs(ts);
        tran.setProcTs(ts);
        tranFile.write(tran.bytes());
    }

    private static void abend(BatchContext ctx) {
        ctx.display("ABENDING PROGRAM");
        throw new Abend(999);
    }
}
