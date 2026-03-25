package com.carddemo.dto;

public class LoginResponse {

    private String token;
    private String userId;
    private String firstName;
    private String lastName;
    private String userType;

    public LoginResponse(String token, String userId, String firstName, String lastName, String userType) {
        this.token = token;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
    }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUserType() { return userType; }
}
