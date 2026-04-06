package com.suitecrm.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts", schema = "account_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255) private String name;
    @Column(name = "account_type", length = 50) private String accountType;
    @Column(name = "industry", length = 100) private String industry;
    @Column(name = "annual_revenue") private BigDecimal annualRevenue;
    @Column(name = "annual_revenue_usdollar") private BigDecimal annualRevenueUsdollar;
    @Column(name = "currency_id") private UUID currencyId;
    @Column(name = "employees", length = 10) private String employees;
    @Column(name = "rating", length = 50) private String rating;
    @Column(name = "phone_office", length = 50) private String phoneOffice;
    @Column(name = "phone_alternate", length = 50) private String phoneAlternate;
    @Column(name = "phone_fax", length = 50) private String phoneFax;
    @Column(name = "website", length = 255) private String website;
    @Column(name = "email1", length = 255) private String email1;
    @Column(name = "email2", length = 255) private String email2;
    @Column(name = "email_opt_out") @Builder.Default private Boolean emailOptOut = false;
    @Column(name = "invalid_email") @Builder.Default private Boolean invalidEmail = false;
    @Column(name = "ownership", length = 100) private String ownership;
    @Column(name = "ticker_symbol", length = 20) private String tickerSymbol;
    @Column(name = "sic_code", length = 20) private String sicCode;
    @Column(name = "parent_id") private UUID parentId;
    @Column(name = "parent_name", length = 255) private String parentName;
    @Column(name = "campaign_id") private UUID campaignId;
    @Column(name = "campaign_name", length = 255) private String campaignName;
    @Column(name = "billing_address_street", length = 255) private String billingAddressStreet;
    @Column(name = "billing_address_city", length = 100) private String billingAddressCity;
    @Column(name = "billing_address_state", length = 100) private String billingAddressState;
    @Column(name = "billing_address_postalcode", length = 20) private String billingAddressPostalcode;
    @Column(name = "billing_address_country", length = 100) private String billingAddressCountry;
    @Column(name = "shipping_address_street", length = 255) private String shippingAddressStreet;
    @Column(name = "shipping_address_city", length = 100) private String shippingAddressCity;
    @Column(name = "shipping_address_state", length = 100) private String shippingAddressState;
    @Column(name = "shipping_address_postalcode", length = 20) private String shippingAddressPostalcode;
    @Column(name = "shipping_address_country", length = 100) private String shippingAddressCountry;
    @Column(name = "description", columnDefinition = "TEXT") private String description;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "modified_user_id") private UUID modifiedUserId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "date_entered", nullable = false, updatable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Column(name = "deleted") @Builder.Default private Boolean deleted = false;
    @Version @Column(name = "version") private Long version;

    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
