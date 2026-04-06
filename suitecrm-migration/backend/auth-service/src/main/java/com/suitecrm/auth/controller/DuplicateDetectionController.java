package com.suitecrm.auth.controller;

import com.suitecrm.auth.engine.DuplicateDetectionEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/duplicates")
@RequiredArgsConstructor
public class DuplicateDetectionController {

    private final DuplicateDetectionEngine duplicateDetectionEngine;

    @PostMapping("/find")
    public ResponseEntity<List<Map<String, Object>>> findDuplicates(
            @RequestParam String module,
            @RequestBody Map<String, Object> record) {
        List<String> matchFields = List.of("name", "email1", "phone_work", "first_name", "last_name");
        List<Map<String, Object>> duplicates = duplicateDetectionEngine.findDuplicates(module, record, matchFields);
        return ResponseEntity.ok(duplicates);
    }

    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeRecords(
            @RequestParam String module,
            @RequestParam UUID primaryId,
            @RequestParam List<UUID> duplicateIds,
            @RequestBody Map<String, String> fieldMapping) {
        Map<String, Object> result = duplicateDetectionEngine.mergeRecords(module, primaryId, duplicateIds, fieldMapping);
        return ResponseEntity.ok(result);
    }
}
