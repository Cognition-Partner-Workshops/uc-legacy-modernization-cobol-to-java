package com.suitecrm.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private UUID id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String firstName;
    private String lastName;
    private String phoneWork;
    private String phoneMobile;
    private String title;
    private String department;
    private String status;
    private Boolean isAdmin;
    private String timezone;
    private String language;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
    private LocalDateTime lastLogin;
    private Set<String> roleNames;
    private Set<String> securityGroupNames;
}
