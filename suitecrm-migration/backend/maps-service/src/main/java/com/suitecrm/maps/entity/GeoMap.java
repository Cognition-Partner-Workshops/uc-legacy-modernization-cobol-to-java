package com.suitecrm.maps.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @Table(name = "geo_maps", schema = "maps_schema")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GeoMap {
    @Id @GeneratedValue(generator = "UUID") @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false) private UUID id;
    @Column(nullable = false, length = 255) private String name;
    @Column(name = "center_lat") private Double centerLat;
    @Column(name = "center_lng") private Double centerLng;
    @Column(name = "zoom_level") private Integer zoomLevel;
    @Column(name = "map_type", length = 50) private String mapType;
    @Column(name = "unit", length = 20) private String unit;
    @Column(name = "module_type", length = 100) private String moduleType;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "assigned_user_id") private UUID assignedUserId;
    @Column(name = "date_entered", nullable = false) private LocalDateTime dateEntered;
    @Column(name = "date_modified") private LocalDateTime dateModified;
    @Builder.Default @Column(nullable = false) private Boolean deleted = false;
    @PrePersist protected void onCreate() { dateEntered = LocalDateTime.now(); dateModified = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { dateModified = LocalDateTime.now(); }
}
