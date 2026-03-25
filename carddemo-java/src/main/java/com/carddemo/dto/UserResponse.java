package com.carddemo.dto;

import com.carddemo.entity.User;

public class UserResponse {

    private String userId;
    private String firstName;
    private String lastName;
    private String userType;

    public UserResponse() {}

    public static UserResponse fromEntity(User user) {
        UserResponse resp = new UserResponse();
        resp.userId = user.getUserId();
        resp.firstName = user.getFirstName();
        resp.lastName = user.getLastName();
        resp.userType = user.getUserType();
        return resp;
    }

    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUserType() { return userType; }
}
