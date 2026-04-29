package com.carddemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuOptionDto {

    private int optionNumber;
    private String label;
    private String apiEndpoint;
    private String httpMethod;
    private String description;
    private String cobolProgram;
    private String cobolTransaction;
    private boolean implemented;
}
