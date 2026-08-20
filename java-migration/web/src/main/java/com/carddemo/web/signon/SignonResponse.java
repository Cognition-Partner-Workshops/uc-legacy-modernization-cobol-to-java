package com.carddemo.web.signon;

public record SignonResponse(
        String userId,
        String firstName,
        String lastName,
        String userType,
        String role,
        String nextProgram) {
}
