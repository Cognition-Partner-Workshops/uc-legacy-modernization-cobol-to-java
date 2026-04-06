package com.suitecrm.quotes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "line_item_groups", schema = "quotes_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LineItemGroup {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "parent_type", length = 100)
    private String parentType;
    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "group_number")
    private Integer groupNumber;

    @Column(name = "total_amount", precision = 26, scale = 6)
    private BigDecimal totalAmount;
    @Column(name = "discount_amount", precision = 26, scale = 6)
    private BigDecimal discountAmount;
    @Column(name = "tax_amount", precision = 26, scale = 6)
    private BigDecimal taxAmount;
    @Column(name = "subtotal_amount", precision = 26, scale = 6)
    private BigDecimal subtotalAmount;

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
