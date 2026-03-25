package com.carddemo.batch.model;

/**
 * Represents a rejected daily transaction with the reason for rejection.
 * Maps to the COBOL DALYREJS output file in CBTRN02C.
 *
 * Validation failure reasons (from COBOL):
 *   100 - INVALID CARD NUMBER FOUND
 *   101 - ACCOUNT RECORD NOT FOUND
 *   102 - OVERLIMIT TRANSACTION
 *   103 - TRANSACTION RECEIVED AFTER ACCT EXPIRATION
 */
public record RejectedTransaction(
        String transactionId,
        String cardNumber,
        int failureReasonCode,
        String failureReasonDescription
) {
}
