package com.carddemo.usersecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignonRequest {

    @NotBlank(message = "Please enter User ID ...")
    @Size(max = 8, message = "User ID must be at most 8 characters")
    private String userId;

    @NotBlank(message = "Please enter Password ...")
    @Size(max = 8, message = "Password must be at most 8 characters")
    private String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
