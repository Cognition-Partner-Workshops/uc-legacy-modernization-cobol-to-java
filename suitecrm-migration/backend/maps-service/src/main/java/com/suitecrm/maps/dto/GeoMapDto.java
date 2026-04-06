package com.suitecrm.maps.dto;
import lombok.*; import java.time.LocalDateTime; import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GeoMapDto {
    private UUID id; private String name; private Double centerLat; private Double centerLng;
    private Integer zoomLevel; private String mapType; private String moduleType; private String description;
    private UUID assignedUserId; private LocalDateTime dateEntered; private LocalDateTime dateModified;
}
