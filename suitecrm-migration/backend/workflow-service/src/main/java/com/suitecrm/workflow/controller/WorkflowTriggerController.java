package com.suitecrm.workflow.controller;

import com.suitecrm.workflow.engine.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflows/trigger")
@RequiredArgsConstructor
public class WorkflowTriggerController {

    private final WorkflowEngine workflowEngine;

    @PostMapping
    public ResponseEntity<Map<String, String>> triggerWorkflows(
            @RequestParam String module,
            @RequestParam(defaultValue = "update") String event,
            @RequestBody Map<String, Object> payload) {

        @SuppressWarnings("unchecked")
        Map<String, Object> record = (Map<String, Object>) payload.getOrDefault("record", payload);
        @SuppressWarnings("unchecked")
        Map<String, Object> previousRecord = (Map<String, Object>) payload.get("previousRecord");

        workflowEngine.triggerWorkflows(module, event, record, previousRecord);
        return ResponseEntity.ok(Map.of("status", "workflows triggered"));
    }
}
