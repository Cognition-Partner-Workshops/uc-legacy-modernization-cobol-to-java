package com.suitecrm.common.exception;

public class BusinessValidationException extends RuntimeException {
    private final String field;
    private final String validationCode;

    public BusinessValidationException(String message) {
        super(message);
        this.field = null;
        this.validationCode = null;
    }

    public BusinessValidationException(String field, String message, String validationCode) {
        super(message);
        this.field = field;
        this.validationCode = validationCode;
    }

    public String getField() { return field; }
    public String getValidationCode() { return validationCode; }
}
