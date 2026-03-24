package com.carddemo.usermanagement.exception;

/**
 * Thrown when a user lookup by ID finds no matching record.
 * Mirrors the DFHRESP(NOTFND) condition in COUSR02C and COUSR03C,
 * which produces "User ID NOT found..." messages.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("User ID NOT found: " + userId);
    }
}
