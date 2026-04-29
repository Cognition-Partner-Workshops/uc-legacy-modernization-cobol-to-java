package com.carddemo.dto;

import java.util.List;

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
public class MenuResponseDto {

    private String userId;
    private String userType;
    private String firstName;
    private String lastName;
    private List<MenuOptionDto> menuOptions;
}
