package com.carddemo.exception;

public class TransactionValidationException extends CardDemoException {
    private final int reasonCode;

    public TransactionValidationException(int reasonCode, String message) {
        super(message);
        this.reasonCode = reasonCode;
    }

    public int getReasonCode() {
        return reasonCode;
    }
}
