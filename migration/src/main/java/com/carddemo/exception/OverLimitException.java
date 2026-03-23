package com.carddemo.exception;

public class OverLimitException extends RuntimeException {
    public OverLimitException(String acctId) {
        super("Transaction exceeds credit limit for account: " + acctId);
    }
}
