package com.cognition.usersecurity.dto;

import com.cognition.usersecurity.model.UserType;

public record SignonResponse(String userId, UserType userType, String userTypeCode, String message) {}
