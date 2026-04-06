package com.suitecrm.auth.controller;

import com.suitecrm.auth.entity.AdminSetting;
import com.suitecrm.auth.service.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminSettingsService adminSettingsService;

    @GetMapping("/settings/{category}")
    public ResponseEntity<List<AdminSetting>> getSettings(@PathVariable String category) {
        return ResponseEntity.ok(adminSettingsService.getCategory(category));
    }

    @GetMapping("/settings/{category}/{name}")
    public ResponseEntity<Map<String, String>> getSetting(@PathVariable String category, @PathVariable String name) {
        String value = adminSettingsService.getSetting(category, name).orElse(null);
        return ResponseEntity.ok(Map.of("category", category, "name", name, "value", value != null ? value : ""));
    }

    @PostMapping("/settings")
    public ResponseEntity<AdminSetting> setSetting(@RequestBody Map<String, String> body) {
        AdminSetting setting = adminSettingsService.setSetting(
            body.get("category"), body.get("name"), body.get("value"));
        return ResponseEntity.ok(setting);
    }
}
