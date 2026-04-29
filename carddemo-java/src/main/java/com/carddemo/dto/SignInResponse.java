package com.carddemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignInResponse {

    private String token;
    private String userId;
    private String firstName;
    private String lastName;
    private String userType;
}
