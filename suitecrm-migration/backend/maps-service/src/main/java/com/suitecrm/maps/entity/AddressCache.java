package com.suitecrm.maps.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "address_cache", schema = "maps_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddressCache {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 512) private String address;
    @Column private Double latitude;
    @Column private Double longitude;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); }
}
