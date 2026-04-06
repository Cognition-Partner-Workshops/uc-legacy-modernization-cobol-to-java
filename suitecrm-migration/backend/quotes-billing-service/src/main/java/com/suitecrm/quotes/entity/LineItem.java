package com.suitecrm.quotes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "line_items", schema = "quotes_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LineItem {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "item_description", columnDefinition = "TEXT")
    private String itemDescription;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "cost_price", precision = 26, scale = 6)
    private BigDecimal costPrice;
    @Column(name = "list_price", precision = 26, scale = 6)
    private BigDecimal listPrice;
    @Column(name = "unit_price", precision = 26, scale = 6)
    private BigDecimal unitPrice;
    @Column(name = "discount_price", precision = 26, scale = 6)
    private BigDecimal discountPrice;
    @Column(name = "discount_amount", precision = 26, scale = 6)
    private BigDecimal discountAmount;
    @Column(name = "tax_amount", precision = 26, scale = 6)
    private BigDecimal taxAmount;
    @Column(name = "total_amount", precision = 26, scale = 6)
    private BigDecimal totalAmount;

    @Column(name = "item_number")
    private Integer itemNumber;

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
