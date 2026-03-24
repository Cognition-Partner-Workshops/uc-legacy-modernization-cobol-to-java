package com.carddemo.usermanagement.dto;

import com.carddemo.usermanagement.entity.User;
import com.carddemo.usermanagement.entity.UserType;

/**
 * DTO for returning user data in API responses.
 * Maps from the JPA User entity, translating the UserType enum to a readable string.
 */
public class UserResponse {

    private String userId;
    private String firstName;
    private String lastName;
    private String userType;

    public UserResponse() {
    }

    public UserResponse(String userId, String firstName, String lastName, String userType) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType() == UserType.ADMIN ? "Admin" : "Regular"
        );
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
