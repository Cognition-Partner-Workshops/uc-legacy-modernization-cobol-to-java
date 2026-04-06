package com.suitecrm.productcatalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products", schema = "product_catalog_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String status;

    @Column(name = "part_number", length = 100)
    private String partNumber;

    @Column(name = "cost_price", precision = 26, scale = 6)
    private BigDecimal costPrice;

    @Column(name = "list_price", precision = 26, scale = 6)
    private BigDecimal listPrice;

    @Column(name = "discount_price", precision = 26, scale = 6)
    private BigDecimal discountPrice;

    @Column(name = "currency_id")
    private UUID currencyId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "product_type_id")
    private UUID productTypeId;

    @Column(name = "manufacturer_id")
    private UUID manufacturerId;

    @Column(name = "weight", precision = 12, scale = 4)
    private BigDecimal weight;

    @Column(name = "qty_in_stock")
    private Integer qtyInStock;

    @Column(name = "date_available")
    private LocalDateTime dateAvailable;

    @Column(name = "date_cost_price")
    private LocalDateTime dateCostPrice;

    @Column(name = "tax_class", length = 50)
    private String taxClass;

    @Column(name = "website")
    private String website;

    @Column(name = "mft_part_num", length = 100)
    private String mftPartNum;

    @Column(name = "vendor_part_num", length = 100)
    private String vendorPartNum;

    @Column(name = "support_name")
    private String supportName;

    @Column(name = "support_description", columnDefinition = "TEXT")
    private String supportDescription;

    @Column(name = "support_contact")
    private String supportContact;

    @Column(name = "support_term", length = 50)
    private String supportTerm;

    @Column(name = "pricing_formula", length = 50)
    private String pricingFormula;

    @Column(name = "pricing_factor")
    private Integer pricingFactor;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "date_entered", nullable = false)
    private LocalDateTime dateEntered;

    @Column(name = "date_modified")
    private LocalDateTime dateModified;

    @Column(nullable = false)
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        dateEntered = LocalDateTime.now();
        dateModified = LocalDateTime.now();
        if (deleted == null) deleted = false;
        if (status == null) status = "Available";
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
