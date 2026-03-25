package com.carddemo.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login request DTO.
 * Maps to COBOL sign-on screen fields in COSGN00C.cbl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank
    @Size(max = 8)
    private String userId;

    @NotBlank
    @Size(max = 8)
    private String password;
}
