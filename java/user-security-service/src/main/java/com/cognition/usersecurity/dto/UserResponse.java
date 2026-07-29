package com.cognition.usersecurity.dto;

import com.cognition.usersecurity.model.User;
import com.cognition.usersecurity.model.UserType;

public record UserResponse(String userId, String firstName, String lastName, UserType userType) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getUserType());
    }
}
