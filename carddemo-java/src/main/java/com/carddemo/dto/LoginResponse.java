package com.carddemo.dto;

/**
 * Login response DTO - replaces COBOL COMMAREA fields set after successful sign-on.
 * Original COBOL: CDEMO-USER-ID, CDEMO-USER-TYPE, then XCTL to menu program.
 */
public class LoginResponse {

    private String userId;
    private String userType;
    private String firstName;
    private String lastName;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(String userId, String userType, String firstName,
                         String lastName, String message) {
        this.userId = userId;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
