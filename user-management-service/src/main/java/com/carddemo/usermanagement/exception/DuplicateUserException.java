package com.carddemo.usermanagement.exception;

/**
 * Thrown when attempting to create a user with an ID that already exists.
 * Mirrors the DFHRESP(DUPREC) / DFHRESP(DUPKEY) condition in COUSR01C,
 * which produces "User ID already exist..." message.
 */
public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String userId) {
        super("User ID already exists: " + userId);
    }
}
