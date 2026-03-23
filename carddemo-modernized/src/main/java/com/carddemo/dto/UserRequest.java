package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * User management request DTO - replaces COBOL COUSR01C/COUSR02C screen input.
 * Legacy: Admin user CRUD operations via BMS maps COUSR01-COUSR03
 */
public class UserRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "[AUau]", message = "User type must be 'A' (Admin) or 'U' (User)")
    private String userType;

    public UserRequest() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}
