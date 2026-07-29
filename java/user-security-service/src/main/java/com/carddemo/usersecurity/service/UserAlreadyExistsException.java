package com.carddemo.usersecurity.service;

/** Mirrors the "User ID already exist..." path of COUSR01C. */
public class UserAlreadyExistsException extends RuntimeException {

    private final String userId;

    public UserAlreadyExistsException(String userId) {
        super("User ID already exist...");
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
