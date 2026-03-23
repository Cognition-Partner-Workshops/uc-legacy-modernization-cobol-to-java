package com.carddemo.dto;

/**
 * Login response DTO - replaces COBOL COSGN00C commarea output.
 * Legacy: CARDDEMO-COMMAREA fields populated after successful sign-on
 */
public class LoginResponse {

    private String userId;
    private String userType;
    private String firstName;
    private String lastName;
    private String token;
    private String message;

    public LoginResponse() {}

    public LoginResponse(String userId, String userType, String firstName,
                         String lastName, String token, String message) {
        this.userId = userId;
        this.userType = userType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
        this.message = message;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
