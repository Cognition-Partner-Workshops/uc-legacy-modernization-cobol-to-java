package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * User create request DTO - replaces BMS map COUSR01 input fields.
 * Maps to COBOL program COUSR01C (Add User, CICS txn CU01).
 */
public class UserCreateRequest {

    @NotBlank(message = "User ID is required")
    @Size(max = 8, message = "User ID must be at most 8 characters")
    private String usrId;

    @Size(max = 20, message = "First name must be at most 20 characters")
    private String usrFirstName;

    @Size(max = 20, message = "Last name must be at most 20 characters")
    private String usrLastName;

    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 8, message = "Password must be between 1 and 8 characters")
    private String usrPwd;

    @NotBlank(message = "User type is required")
    @Pattern(regexp = "[AU]", message = "User type must be 'A' (Admin) or 'U' (User)")
    private String usrType;

    public UserCreateRequest() {
    }

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    public String getUsrFirstName() {
        return usrFirstName;
    }

    public void setUsrFirstName(String usrFirstName) {
        this.usrFirstName = usrFirstName;
    }

    public String getUsrLastName() {
        return usrLastName;
    }

    public void setUsrLastName(String usrLastName) {
        this.usrLastName = usrLastName;
    }

    public String getUsrPwd() {
        return usrPwd;
    }

    public void setUsrPwd(String usrPwd) {
        this.usrPwd = usrPwd;
    }

    public String getUsrType() {
        return usrType;
    }

    public void setUsrType(String usrType) {
        this.usrType = usrType;
    }
}
