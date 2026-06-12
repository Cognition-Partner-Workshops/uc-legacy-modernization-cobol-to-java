package com.cardemo.auth.dto;

public record LoginResponse(
        String token,
        String userId,
        String userType
) {
}
