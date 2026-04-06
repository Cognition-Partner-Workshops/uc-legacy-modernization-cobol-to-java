package com.suitecrm.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String firstName;
    private String lastName;
    private String title;
    private String department;
    private String phoneWork;
    private String phoneMobile;
    private String status;
    private Set<String> roles;
    private Set<UUID> securityGroupIds;
    private UUID reportsToId;
}
