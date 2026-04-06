package com.suitecrm.productcatalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_bundles", schema = "product_catalog_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductBundle {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "bundle_stage", length = 50)
    private String bundleStage;

    @Column(name = "currency_id")
    private UUID currencyId;

    @Column(name = "base_rate", precision = 26, scale = 6)
    private BigDecimal baseRate;

    @Column(name = "shipping", precision = 26, scale = 6)
    private BigDecimal shipping;

    @Column(name = "tax", precision = 26, scale = 6)
    private BigDecimal tax;

    @Column(name = "subtotal", precision = 26, scale = 6)
    private BigDecimal subtotal;

    @Column(name = "total", precision = 26, scale = 6)
    private BigDecimal total;

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
    }

    @PreUpdate
    protected void onUpdate() {
        dateModified = LocalDateTime.now();
    }
}
