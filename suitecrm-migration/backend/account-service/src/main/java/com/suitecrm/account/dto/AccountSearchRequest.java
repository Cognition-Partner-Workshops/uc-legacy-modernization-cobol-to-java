package com.suitecrm.account.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSearchRequest {
    private String name;
    private String accountType;
    private String industry;
    private String rating;
    private String phoneOffice;
    private String email;
    private String billingAddressCity;
    private String billingAddressState;
    private String billingAddressCountry;
    private UUID assignedUserId;
    private UUID parentId;
    private BigDecimal minRevenue;
    private BigDecimal maxRevenue;
}
