package com.suitecrm.auth.controller;

import com.suitecrm.auth.engine.AuditTrailService;
import com.suitecrm.auth.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditTrailService auditTrailService;

    @GetMapping("/{module}/{recordId}")
    public ResponseEntity<List<AuditLog>> getAuditTrail(
            @PathVariable String module,
            @PathVariable UUID recordId) {
        return ResponseEntity.ok(auditTrailService.getAuditTrail(recordId, module));
    }
}
