package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "User ID is required")
    @Size(max = 8, message = "User ID must be at most 8 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "User ID must be alphanumeric")
    private String userId;

    @NotBlank(message = "First name is required")
    @Size(max = 20, message = "First name must be at most 20 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 20, message = "Last name must be at most 20 characters")
    private String lastName;

    @NotBlank(message = "Password is required")
    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "^[AU]$", message = "User type must be 'A' (Admin) or 'U' (Regular)")
    private String userType;
}
