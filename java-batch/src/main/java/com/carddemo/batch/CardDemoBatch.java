package com.carddemo.batch;

import com.carddemo.batch.job.InterestCalculationJob;
import com.carddemo.batch.job.StatementGenerationJob;
import com.carddemo.batch.job.TransactionPostingJob;
import com.carddemo.batch.job.TransactionReportJob;

/**
 * Main entry point for the CardDemo Batch Processing System.
 *
 * This is the Java 17+ equivalent of the COBOL batch programs from the
 * CardDemo mainframe application. The following batch programs have been
 * migrated:
 *
 *   CBTRN02C.CBL -> TransactionPostingJob.java
 *     Posts daily transaction records after validation against cross-reference
 *     and account files. Rejects invalid transactions with reason codes.
 *
 *   CBACT04C.CBL -> InterestCalculationJob.java
 *     Calculates monthly interest on transaction category balances using
 *     disclosure group interest rates. Updates account balances.
 *
 *   CBSTM03A.CBL -> StatementGenerationJob.java
 *     Generates account statements in plain text and HTML formats,
 *     listing customer details and transaction summaries.
 *
 *   CBTRN03C.CBL -> TransactionReportJob.java
 *     Produces a daily transaction detail report filtered by date range,
 *     with page totals, account totals, and grand totals.
 *
 * Data structures have been migrated from COBOL copybooks to Java records/POJOs:
 *   CVACT01Y -> AccountRecord.java
 *   CVACT03Y -> CardXrefRecord.java
 *   CVTRA01Y -> TranCatBalRecord.java
 *   CVTRA02Y -> DisclosureGroupRecord.java
 *   CVTRA03Y -> TransactionTypeRecord.java
 *   CVTRA04Y -> TransactionCategoryRecord.java
 *   CVTRA05Y / CVTRA06Y -> TransactionRecord.java
 *   CUSTREC  -> CustomerRecord.java
 *
 * Usage:
 *   java -jar carddemo-batch.jar <job-name> [parameters...]
 *
 *   Jobs:
 *     post-transactions  - Post daily transactions (equivalent to POSTTRAN JCL)
 *     calc-interest      - Calculate interest (equivalent to INTCALC JCL)
 *     gen-statements     - Generate statements (equivalent to CREASTMT JCL)
 *     gen-report         - Generate transaction report (equivalent to TRANREPT JCL)
 */
public class CardDemoBatch {

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        String jobName = args[0];
        System.out.println("CardDemo Batch Processing System (Java 17+)");
        System.out.println("Job: " + jobName);
        System.out.println("=".repeat(60));

        switch (jobName) {
            case "post-transactions" -> {
                System.out.println("Transaction Posting Job");
                System.out.println("Equivalent to: CBTRN02C.CBL / POSTTRAN JCL");
                System.out.println("Note: Requires data files to be loaded. Run with test data.");
            }
            case "calc-interest" -> {
                System.out.println("Interest Calculation Job");
                System.out.println("Equivalent to: CBACT04C.CBL / INTCALC JCL");
                if (args.length < 2) {
                    System.err.println("Error: calc-interest requires a date parameter (YYYY-MM-DD)");
                    System.exit(1);
                }
                System.out.println("Date parameter: " + args[1]);
            }
            case "gen-statements" -> {
                System.out.println("Statement Generation Job");
                System.out.println("Equivalent to: CBSTM03A.CBL / CREASTMT JCL");
            }
            case "gen-report" -> {
                System.out.println("Transaction Report Job");
                System.out.println("Equivalent to: CBTRN03C.CBL / TRANREPT JCL");
                if (args.length < 3) {
                    System.err.println("Error: gen-report requires start-date and end-date parameters");
                    System.exit(1);
                }
                System.out.println("Date range: " + args[1] + " to " + args[2]);
            }
            default -> {
                System.err.println("Unknown job: " + jobName);
                printUsage();
                System.exit(1);
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar carddemo-batch.jar <job-name> [parameters...]");
        System.out.println();
        System.out.println("Available jobs:");
        System.out.println("  post-transactions           Post daily transactions (CBTRN02C)");
        System.out.println("  calc-interest <date>        Calculate interest (CBACT04C)");
        System.out.println("  gen-statements              Generate account statements (CBSTM03A)");
        System.out.println("  gen-report <start> <end>    Generate transaction report (CBTRN03C)");
    }
}
