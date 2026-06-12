package com.cardemo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 8) String userId,
        @NotBlank @Size(max = 20) String firstName,
        @NotBlank @Size(max = 20) String lastName,
        @NotBlank @Size(max = 72) String password,
        @NotBlank @Pattern(regexp = "ADMIN|USER") String userType
) {
}
