package com.suitecrm.maps.service;

import com.suitecrm.maps.dto.*;
import com.suitecrm.maps.entity.*;
import com.suitecrm.maps.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class MapsService {
    private final GeoMapRepository geoMapRepository;
    private final MarkerRepository markerRepository;
    private final AreaRepository areaRepository;

    public Page<GeoMapDto> getAllMaps(Pageable pageable) {
        return geoMapRepository.findByDeletedFalse(pageable).map(this::toMapDto);
    }
    public Page<MarkerDto> getAllMarkers(Pageable pageable) {
        return markerRepository.findByDeletedFalse(pageable).map(this::toMarkerDto);
    }
    public MarkerDto getMarkerById(UUID id) {
        return toMarkerDto(markerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Marker not found: " + id)));
    }

    private GeoMapDto toMapDto(GeoMap m) {
        return GeoMapDto.builder().id(m.getId()).name(m.getName()).centerLat(m.getCenterLat()).centerLng(m.getCenterLng())
                .zoomLevel(m.getZoomLevel()).mapType(m.getMapType()).moduleType(m.getModuleType())
                .description(m.getDescription()).assignedUserId(m.getAssignedUserId())
                .dateEntered(m.getDateEntered()).dateModified(m.getDateModified()).build();
    }
    private MarkerDto toMarkerDto(Marker m) {
        return MarkerDto.builder().id(m.getId()).name(m.getName()).city(m.getCity()).state(m.getState())
                .country(m.getCountry()).latitude(m.getLatitude()).longitude(m.getLongitude())
                .markerType(m.getMarkerType()).relatedModule(m.getRelatedModule()).relatedId(m.getRelatedId())
                .description(m.getDescription()).assignedUserId(m.getAssignedUserId())
                .dateEntered(m.getDateEntered()).dateModified(m.getDateModified()).build();
    }
}
