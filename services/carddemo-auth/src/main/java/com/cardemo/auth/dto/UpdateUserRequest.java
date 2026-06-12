package com.cardemo.auth.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 20) String firstName,
        @Size(max = 20) String lastName,
        String password,
        @jakarta.validation.constraints.Pattern(regexp = "ADMIN|USER") String userType
) {
}
