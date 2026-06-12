package com.cardemo.auth.dto;

import com.cardemo.auth.entity.User;

public record UserResponse(
        String userId,
        String firstName,
        String lastName,
        String userType
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType().name()
        );
    }
}
