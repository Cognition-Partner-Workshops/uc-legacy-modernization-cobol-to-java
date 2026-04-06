package com.suitecrm.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "currencies", schema = "auth_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Currency {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(nullable = false, length = 50) private String symbol;
    @Column(name = "iso4217", nullable = false, length = 3) private String iso4217;
    @Column(name = "conversion_rate", precision = 26, scale = 6) private BigDecimal conversionRate;
    @Column(length = 100) private String status;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
