package com.suitecrm.maps.controller;

import com.suitecrm.maps.dto.*;
import com.suitecrm.maps.service.MapsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/maps") @RequiredArgsConstructor
public class MapsController {
    private final MapsService mapsService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP')")
    public ResponseEntity<Page<GeoMapDto>> getAllMaps(Pageable pageable) { return ResponseEntity.ok(mapsService.getAllMaps(pageable)); }

    @GetMapping("/markers")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP')")
    public ResponseEntity<Page<MarkerDto>> getAllMarkers(Pageable pageable) { return ResponseEntity.ok(mapsService.getAllMarkers(pageable)); }

    @GetMapping("/markers/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_SALES_REP')")
    public ResponseEntity<MarkerDto> getMarkerById(@PathVariable UUID id) { return ResponseEntity.ok(mapsService.getMarkerById(id)); }
}
