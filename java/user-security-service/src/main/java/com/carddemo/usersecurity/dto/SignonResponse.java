package com.carddemo.usersecurity.dto;

import com.carddemo.usersecurity.domain.User;

/**
 * Result of a successful signon. {@code nextProgram} records the CICS program
 * COSGN00C would have transferred control to (COADM01C for admins, COMEN01C otherwise).
 */
public record SignonResponse(UserResponse user, boolean admin, String nextProgram) {

    public static SignonResponse from(User user) {
        return new SignonResponse(UserResponse.from(user), user.isAdmin(),
                user.isAdmin() ? "COADM01C" : "COMEN01C");
    }
}
