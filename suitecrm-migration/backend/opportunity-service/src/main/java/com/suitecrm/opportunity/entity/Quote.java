package com.suitecrm.opportunity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quotes", schema = "opportunity_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quote {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "quote_num", length = 50)
    private String quoteNum;

    @Column(name = "quote_stage", length = 50)
    private String quoteStage;

    @Column(name = "purchase_order_num", length = 100)
    private String purchaseOrderNum;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "shipping_amount")
    private BigDecimal shippingAmount;

    @Column(name = "total")
    private BigDecimal total;

    @Column(name = "grand_total")
    private BigDecimal grandTotal;

    @Column(name = "currency_id")
    private UUID currencyId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "opportunity_id")
    private UUID opportunityId;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "billing_address_street", length = 255)
    private String billingAddressStreet;

    @Column(name = "billing_address_city", length = 100)
    private String billingAddressCity;

    @Column(name = "billing_address_state", length = 100)
    private String billingAddressState;

    @Column(name = "billing_address_postalcode", length = 20)
    private String billingAddressPostalcode;

    @Column(name = "billing_address_country", length = 100)
    private String billingAddressCountry;

    @Column(name = "shipping_address_street", length = 255)
    private String shippingAddressStreet;

    @Column(name = "shipping_address_city", length = 100)
    private String shippingAddressCity;

    @Column(name = "shipping_address_state", length = 100)
    private String shippingAddressState;

    @Column(name = "shipping_address_postalcode", length = 20)
    private String shippingAddressPostalcode;

    @Column(name = "shipping_address_country", length = 100)
    private String shippingAddressCountry;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "date_entered", nullable = false, updatable = false)
    private LocalDateTime dateEntered;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        dateModified = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
