package com.suitecrm.maps.dto;
import lombok.*; import java.time.LocalDateTime; import java.util.UUID;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MarkerDto {
    private UUID id; private String name; private String city; private String state; private String country;
    private Double latitude; private Double longitude; private String markerType;
    private String relatedModule; private UUID relatedId; private String description;
    private UUID assignedUserId; private LocalDateTime dateEntered; private LocalDateTime dateModified;
}
