package com.carddemo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequest {

    @NotBlank(message = "Please enter User ID ...")
    private String userId;

    @NotBlank(message = "Please enter Password ...")
    private String password;
}
