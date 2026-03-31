package com.carddemo.dto;

import lombok.Data;

@Data
public class CardUpdateRequest {
    private String cardNum;
    private String embossedName;
    private String expirationDate;
    private String activeStatus;
}
