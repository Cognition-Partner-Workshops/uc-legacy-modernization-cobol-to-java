package com.carddemo.usersecurity.service;

/** Mirrors the "Wrong Password. Try again ..." path of COSGN00C. */
public class WrongPasswordException extends RuntimeException {

    public WrongPasswordException() {
        super("Wrong Password. Try again ...");
    }
}
