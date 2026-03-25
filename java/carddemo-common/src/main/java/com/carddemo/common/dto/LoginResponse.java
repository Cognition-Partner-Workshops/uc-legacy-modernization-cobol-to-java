package com.carddemo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login response DTO.
 * Maps to COBOL sign-on response from COSGN00C.cbl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String userId;
    private String userType;
    private String token;
    private String message;
}
