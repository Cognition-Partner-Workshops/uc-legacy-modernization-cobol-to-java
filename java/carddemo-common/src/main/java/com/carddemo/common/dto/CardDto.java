package com.carddemo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Data Transfer Object for Card entity.
 * Maps to COBOL copybook: CVACT02Y.cpy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private String cardNumber;
    private Long accountId;
    private int cvvCode;
    private String embossedName;
    private LocalDate expirationDate;
    private String activeStatus;
}
