package carddemo.programs;

import carddemo.JobContext;
import carddemo.cobol.Bytes;
import carddemo.cobol.FixedFile;
import carddemo.cobol.KeyedFile;
import carddemo.records.AccountRecord;
import carddemo.records.CardXrefRecord;
import carddemo.records.DisGroupRecord;
import carddemo.records.TranCatBalRecord;
import carddemo.records.TranRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * CBACT04C - monthly interest calculation. Walks the transaction-category balance file in key
 * order, looks up each account's disclosure group interest rate (falling back to the DEFAULT
 * group), writes one interest transaction per category and, at each account boundary, posts the
 * accumulated interest to the account balance and resets the cycle credit/debit.
 *
 * <p>The COBOL main loop is {@code PERFORM UNTIL END-OF-FILE = 'Y'} whose ELSE branch (meant to
 * post the last account after end-of-file) can never execute, so the final account's interest
 * is written as transactions but never applied to its master record. That behaviour is
 * reproduced here because the golden outputs depend on it.
 *
 * <p>PARM is {@code YYYYMMDDHH}; the interest transaction ids are PARM followed by a 6-digit
 * running sequence number.
 */
public final class Cbact04c implements BatchProgram {
    static final String DEFAULT_GROUP = "DEFAULT";
    private static final BigDecimal MONTHS_TIMES_PERCENT = new BigDecimal("1200");
    private static final int INTEREST_DIGITS = 11;
    private static final int INTEREST_SCALE = 2;

    /**
     * {@code COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200} stored into
     * {@code PIC S9(09)V99} without ROUNDED: the quotient is truncated to two decimals.
     */
    public static BigDecimal monthlyInterest(BigDecimal categoryBalance, BigDecimal annualRatePercent) {
        BigDecimal quotient = categoryBalance.multiply(annualRatePercent)
                .divide(MONTHS_TIMES_PERCENT, INTEREST_SCALE, RoundingMode.DOWN);
        return storeSigned(quotient);
    }

    /** Store into {@code PIC S9(09)V99}: drop excess decimals and high-order digits. */
    static BigDecimal storeSigned(BigDecimal value) {
        BigDecimal truncated = value.setScale(INTEREST_SCALE, RoundingMode.DOWN);
        BigDecimal limit = BigDecimal.TEN.pow(INTEREST_DIGITS - INTEREST_SCALE);
        BigDecimal magnitude = truncated.abs().remainder(limit);
        return truncated.signum() < 0 ? magnitude.negate() : magnitude;
    }

    /** {@code 1050-UPDATE-ACCOUNT}: add the account's total interest and zero the cycle counters. */
    public static void applyInterest(AccountRecord acct, BigDecimal totalInterest) {
        acct.set(AccountRecord.CURR_BAL, acct.get(AccountRecord.CURR_BAL).add(totalInterest));
        acct.set(AccountRecord.CURR_CYC_CREDIT, 0);
        acct.set(AccountRecord.CURR_CYC_DEBIT, 0);
    }

    /** {@code 1300-B-WRITE-TX}: builds the interest transaction for one category balance. */
    public static void buildInterestTransaction(TranRecord tran, String parmDate, int suffix,
            AccountRecord acct, CardXrefRecord xref, BigDecimal monthlyInterest, String timestamp) {
        tran.set(TranRecord.ID, parmDate + String.format("%06d", suffix));
        tran.set(TranRecord.TYPE_CD, "01");
        tran.set(TranRecord.CAT_CD, 5);
        tran.set(TranRecord.SOURCE, "System");
        tran.set(TranRecord.DESC, "Int. for a/c " + acct.getText(AccountRecord.ACCT_ID_TEXT));
        tran.set(TranRecord.AMT, monthlyInterest);
        tran.set(TranRecord.MERCHANT_ID, 0);
        tran.set(TranRecord.MERCHANT_NAME, "");
        tran.set(TranRecord.MERCHANT_CITY, "");
        tran.set(TranRecord.MERCHANT_ZIP, "");
        tran.set(TranRecord.CARD_NUM, xref.get(CardXrefRecord.CARD_NUM));
        tran.set(TranRecord.ORIG_TS, timestamp);
        tran.set(TranRecord.PROC_TS, timestamp);
    }

    @Override
    public int run(JobContext job) {
        job.display("START OF EXECUTION OF PROGRAM CBACT04C");
        String parmDate = job.parm() == null ? "" : job.parm();
        if (parmDate.length() != 10) {
            throw new IllegalArgumentException("PARM must be YYYYMMDDHH, got '" + parmDate + "'");
        }

        KeyedFile tcatbal = KeyedFile.load(job.dd("TCATBALF"), TranCatBalRecord.LENGTH,
                TranCatBalRecord.KEY_OFFSET, TranCatBalRecord.KEY_LENGTH);
        KeyedFile xrefFile = KeyedFile.load(job.dd("XREFFILE"), CardXrefRecord.LENGTH,
                CardXrefRecord.KEY_OFFSET, CardXrefRecord.KEY_LENGTH);
        KeyedFile discgrp = KeyedFile.load(job.dd("DISCGRP"), DisGroupRecord.LENGTH,
                DisGroupRecord.KEY_OFFSET, DisGroupRecord.KEY_LENGTH);
        KeyedFile acctFile = KeyedFile.load(job.dd("ACCTFILE"), AccountRecord.LENGTH,
                AccountRecord.KEY_OFFSET, AccountRecord.KEY_LENGTH);
        Map<byte[], byte[]> xrefByAccount = alternateIndex(xrefFile);

        TranCatBalRecord catBal = new TranCatBalRecord();
        AccountRecord acct = new AccountRecord();
        CardXrefRecord xref = new CardXrefRecord();
        DisGroupRecord disGroup = new DisGroupRecord();
        TranRecord tran = new TranRecord();

        byte[] lastAcctNum = Bytes.spaces(AccountRecord.KEY_LENGTH);
        boolean firstTime = true;
        BigDecimal totalInterest = BigDecimal.ZERO;
        int tranIdSuffix = 0;

        try (FixedFile.Writer tranFile = new FixedFile.Writer(job.dd("TRANSACT"))) {
            for (byte[] image : tcatbal.records()) {
                catBal.load(image);
                job.display(catBal.bytes());
                byte[] acctId = catBal.get(TranCatBalRecord.ACCT_ID_TEXT);
                if (!Arrays.equals(acctId, lastAcctNum)) {
                    if (!firstTime) {
                        updateAccount(job, acctFile, acct, totalInterest);
                    } else {
                        firstTime = false;
                    }
                    totalInterest = BigDecimal.ZERO;
                    lastAcctNum = acctId;
                    acctFile.read(acctId).ifPresentOrElse(acct::load,
                            () -> job.display("ACCOUNT NOT FOUND: ", acctId));
                    byte[] xrefImage = xrefByAccount.get(acctId);
                    if (xrefImage != null) {
                        xref.load(xrefImage);
                    } else {
                        job.display("ACCOUNT NOT FOUND: ", acctId);
                    }
                }

                BigDecimal rate = interestRate(job, discgrp, disGroup, acct.get(AccountRecord.GROUP_ID),
                        catBal.get(TranCatBalRecord.TYPE_CD), catBal.get(TranCatBalRecord.CAT_CD_TEXT));
                if (rate.signum() != 0) {
                    BigDecimal monthly = monthlyInterest(catBal.get(TranCatBalRecord.BAL), rate);
                    totalInterest = storeSigned(totalInterest.add(monthly));
                    tranIdSuffix++;
                    buildInterestTransaction(tran, parmDate, tranIdSuffix, acct, xref, monthly, job.timestamp());
                    tranFile.write(tran.bytes());
                }
            }
        }
        acctFile.save(job.dd("ACCTFILE"));
        job.display("END OF EXECUTION OF PROGRAM CBACT04C");
        return 0;
    }

    /** XREF alternate index on account id; with duplicates the lowest card number wins (primary-key order). */
    private static Map<byte[], byte[]> alternateIndex(KeyedFile xrefFile) {
        Map<byte[], byte[]> index = new TreeMap<>(Arrays::compareUnsigned);
        for (byte[] record : xrefFile.records()) {
            index.putIfAbsent(Bytes.slice(record, CardXrefRecord.ALT_KEY_OFFSET, CardXrefRecord.ALT_KEY_LENGTH),
                    record);
        }
        return index;
    }

    /** {@code 1200-GET-INTEREST-RATE} with the {@code DEFAULT} group fallback. */
    private static BigDecimal interestRate(JobContext job, KeyedFile discgrp, DisGroupRecord disGroup,
            byte[] groupId, byte[] typeCd, byte[] catCd) {
        Optional<byte[]> found = discgrp.read(disGroupKey(groupId, typeCd, catCd));
        if (found.isEmpty()) {
            job.display("DISCLOSURE GROUP RECORD MISSING");
            job.display("TRY WITH DEFAULT GROUP CODE");
            byte[] defaultGroup = Bytes.spaces(DisGroupRecord.ACCT_GROUP_ID.length());
            Bytes.moveAlnum(DEFAULT_GROUP, defaultGroup, 0, defaultGroup.length);
            found = discgrp.read(disGroupKey(defaultGroup, typeCd, catCd));
            if (found.isEmpty()) {
                throw Abend.ioError(job, "ERROR READING DEFAULT DISCLOSURE GROUP", "23");
            }
        }
        disGroup.load(found.get());
        return disGroup.get(DisGroupRecord.INT_RATE);
    }

    private static byte[] disGroupKey(byte[] groupId, byte[] typeCd, byte[] catCd) {
        byte[] key = new byte[DisGroupRecord.KEY_LENGTH];
        Bytes.moveAlnum(groupId, key, DisGroupRecord.ACCT_GROUP_ID.offset(), DisGroupRecord.ACCT_GROUP_ID.length());
        Bytes.moveAlnum(typeCd, key, DisGroupRecord.TRAN_TYPE_CD.offset(), DisGroupRecord.TRAN_TYPE_CD.length());
        Bytes.moveAlnum(catCd, key, DisGroupRecord.TRAN_CAT_CD_TEXT.offset(), DisGroupRecord.TRAN_CAT_CD_TEXT.length());
        return key;
    }

    private static void updateAccount(JobContext job, KeyedFile acctFile, AccountRecord acct, BigDecimal totalInterest) {
        applyInterest(acct, totalInterest);
        byte[] image = acct.bytes();
        if (!acctFile.contains(acctFile.keyOf(image))) {
            throw Abend.ioError(job, "ERROR RE-WRITING ACCOUNT FILE", "23");
        }
        acctFile.rewrite(image);
    }
}
