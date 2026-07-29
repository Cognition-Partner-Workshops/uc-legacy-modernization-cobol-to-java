package com.carddemo.usersecurity.dto;

import com.carddemo.usersecurity.domain.User;

/** Response view of a USRSEC record; the password is never exposed. */
public record UserResponse(String userId, String firstName, String lastName, String userType) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getFirstName(), user.getLastName(), user.getUserType());
    }
}
