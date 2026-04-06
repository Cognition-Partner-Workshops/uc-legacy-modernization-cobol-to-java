package com.suitecrm.auth.controller;

import com.suitecrm.auth.engine.ImportEngine;
import com.suitecrm.auth.engine.ImportResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/import-export")
@RequiredArgsConstructor
public class ImportExportController {

    private final ImportEngine importEngine;

    @PostMapping("/import")
    public ResponseEntity<ImportResult> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("importMapId") UUID importMapId) throws Exception {
        ImportResult result = importEngine.importCsv(file, importMapId);
        return ResponseEntity.ok(result);
    }
}
