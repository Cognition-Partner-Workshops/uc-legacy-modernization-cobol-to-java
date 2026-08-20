package com.carddemo.batch.ebcdic;

public record UsrsecRecord(
        String userId,
        String firstName,
        String lastName,
        String password,
        String userType) {
}
