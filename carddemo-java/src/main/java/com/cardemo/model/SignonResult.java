package com.cardemo.model;

/**
 * Represents the result of a signon attempt.
 */
public class SignonResult {

    public enum Status {
        SUCCESS,
        USER_NOT_FOUND,
        WRONG_PASSWORD,
        EMPTY_USER_ID,
        EMPTY_PASSWORD,
        SYSTEM_ERROR
    }

    private final Status status;
    private final String message;
    private final UserSecurity user;

    private SignonResult(Status status, String message, UserSecurity user) {
        this.status = status;
        this.message = message;
        this.user = user;
    }

    public static SignonResult success(UserSecurity user) {
        return new SignonResult(Status.SUCCESS, "Sign on successful", user);
    }

    public static SignonResult emptyUserId() {
        return new SignonResult(Status.EMPTY_USER_ID,
                "Please enter User ID ...", null);
    }

    public static SignonResult emptyPassword() {
        return new SignonResult(Status.EMPTY_PASSWORD,
                "Please enter Password ...", null);
    }

    public static SignonResult userNotFound() {
        return new SignonResult(Status.USER_NOT_FOUND,
                "User not found. Try again ...", null);
    }

    public static SignonResult wrongPassword() {
        return new SignonResult(Status.WRONG_PASSWORD,
                "Wrong Password. Try again ...", null);
    }

    public static SignonResult systemError() {
        return new SignonResult(Status.SYSTEM_ERROR,
                "Unable to verify the User ...", null);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public UserSecurity getUser() {
        return user;
    }

    public boolean isSuccessful() {
        return status == Status.SUCCESS;
    }
}
