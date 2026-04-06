package com.suitecrm.maps.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "areas", schema = "maps_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Area {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "city", length = 100) private String city;
    @Column(name = "state", length = 100) private String state;
    @Column(name = "country", length = 100) private String country;
    @Column(name = "coordinates", columnDefinition = "TEXT") private String coordinates;
    @Column(name = "area_type", length = 100) private String areaType;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
