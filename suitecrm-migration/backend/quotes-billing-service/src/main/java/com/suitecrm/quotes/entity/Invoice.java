package com.suitecrm.quotes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "quotes_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Invoice {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "quote_id")
    private UUID quoteId;

    @Column(length = 100)
    private String status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "subtotal_amount", precision = 26, scale = 6)
    private BigDecimal subtotalAmount;
    @Column(name = "discount_amount", precision = 26, scale = 6)
    private BigDecimal discountAmount;
    @Column(name = "tax_amount", precision = 26, scale = 6)
    private BigDecimal taxAmount;
    @Column(name = "shipping_amount", precision = 26, scale = 6)
    private BigDecimal shippingAmount;
    @Column(name = "total_amount", precision = 26, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "currency_id")
    private UUID currencyId;

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

    @Column(name = "account_id")
    private UUID accountId;
    @Column(name = "contact_id")
    private UUID contactId;
    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;
    @Column(name = "date_modified")
    private LocalDateTime dateModified;
    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
