package com.carddemo.card.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCardRequest {
    private String embossedName;
    private Character activeStatus;
    private Integer expirationMonth;
    private Integer expirationYear;

    @NotNull(message = "version is required for optimistic locking")
    private Long version;
}
