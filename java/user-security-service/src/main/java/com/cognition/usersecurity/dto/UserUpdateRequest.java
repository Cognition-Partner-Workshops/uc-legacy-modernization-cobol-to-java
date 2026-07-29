package com.cognition.usersecurity.dto;

import com.cognition.usersecurity.model.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "First Name can NOT be empty...") @Size(max = 20) String firstName,
        @NotBlank(message = "Last Name can NOT be empty...") @Size(max = 20) String lastName,
        @NotBlank(message = "Password can NOT be empty...") @Size(max = 9) String password,
        @NotNull(message = "User Type can NOT be empty...") UserType userType) {}
