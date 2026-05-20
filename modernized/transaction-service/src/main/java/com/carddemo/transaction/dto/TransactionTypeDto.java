package com.carddemo.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionTypeDto {
    @NotBlank(message = "tranType is required") private String tranType;
    private String typeDesc;
}
