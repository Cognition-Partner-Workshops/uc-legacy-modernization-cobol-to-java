package com.carddemo.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing user (replaces COUSR02C input validation).
 *
 * <p>Validation rules replicate the COBOL UPDATE-USER-INFO logic from COUSR02C.cbl
 * (lines 179-213), where first name, last name, password, and user type are all required.
 * Note: userId is taken from the path variable, not the request body.
 */
public class UpdateUserRequest {

    @NotBlank(message = "First Name can NOT be empty...")
    @Size(max = 20, message = "First Name must be at most 20 characters")
    private String firstName;

    @NotBlank(message = "Last Name can NOT be empty...")
    @Size(max = 20, message = "Last Name must be at most 20 characters")
    private String lastName;

    @NotBlank(message = "Password can NOT be empty...")
    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    @NotBlank(message = "User Type can NOT be empty...")
    @Pattern(regexp = "(?i)[RA]", message = "User Type must be 'R' (Regular) or 'A' (Admin)")
    private String userType;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String firstName, String lastName, String password, String userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userType = userType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
