package com.suitecrm.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    private String name;

    @Size(max = 50, message = "Account type must not exceed 50 characters")
    private String accountType;

    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    private BigDecimal annualRevenue;

    @Size(max = 10)
    private String employees;

    @Size(max = 50)
    private String rating;

    @Size(max = 50)
    private String phoneOffice;

    @Size(max = 50)
    private String phoneAlternate;

    @Size(max = 50)
    private String phoneFax;

    @Size(max = 255)
    private String website;

    @Size(max = 255)
    private String email;

    @Size(max = 100)
    private String ownership;

    @Size(max = 20)
    private String tickerSymbol;

    @Size(max = 20)
    private String sicCode;

    private UUID parentId;

    @Size(max = 255)
    private String billingAddressStreet;
    @Size(max = 100)
    private String billingAddressCity;
    @Size(max = 100)
    private String billingAddressState;
    @Size(max = 20)
    private String billingAddressPostalcode;
    @Size(max = 100)
    private String billingAddressCountry;

    @Size(max = 255)
    private String shippingAddressStreet;
    @Size(max = 100)
    private String shippingAddressCity;
    @Size(max = 100)
    private String shippingAddressState;
    @Size(max = 20)
    private String shippingAddressPostalcode;
    @Size(max = 100)
    private String shippingAddressCountry;

    private String description;
    private UUID assignedUserId;
    private UUID campaignId;
}
