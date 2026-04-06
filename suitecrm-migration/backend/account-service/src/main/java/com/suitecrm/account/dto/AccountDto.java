package com.suitecrm.account.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private UUID id;
    private String name;
    private String accountType;
    private String industry;
    private BigDecimal annualRevenue;
    private String employees;
    private String rating;
    private String phoneOffice;
    private String phoneAlternate;
    private String phoneFax;
    private String website;
    private String email;
    private String ownership;
    private String tickerSymbol;
    private String sicCode;
    private UUID parentId;
    private String parentName;
    private String billingAddressStreet;
    private String billingAddressCity;
    private String billingAddressState;
    private String billingAddressPostalcode;
    private String billingAddressCountry;
    private String shippingAddressStreet;
    private String shippingAddressCity;
    private String shippingAddressState;
    private String shippingAddressPostalcode;
    private String shippingAddressCountry;
    private String description;
    private UUID assignedUserId;
    private String assignedUserName;
    private UUID createdBy;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
