package com.carddemo.usersecurity.service;

/** Mirrors the CICS RESP 13 (NOTFND) path of the USRSEC programs. */
public class UserNotFoundException extends RuntimeException {

    private final String userId;

    public UserNotFoundException(String userId) {
        super("User ID NOT found...");
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
