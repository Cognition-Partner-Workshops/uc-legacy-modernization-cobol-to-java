package com.cognition.usersecurity.dto;

import jakarta.validation.constraints.NotBlank;

public record SignonRequest(@NotBlank(message = "Please enter User ID ...") String userId,
                            @NotBlank(message = "Please enter Password ...") String password) {}
