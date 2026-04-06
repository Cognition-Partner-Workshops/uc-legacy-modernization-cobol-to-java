package com.suitecrm.document.controller;

import com.suitecrm.document.entity.PdfTemplate;
import com.suitecrm.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pdf-templates")
@RequiredArgsConstructor
public class PdfTemplateController {

    private final DocumentService documentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<Page<PdfTemplate>> listTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(documentService.listPdfTemplates(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<PdfTemplate> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getPdfTemplate(id));
    }

    @GetMapping("/by-module/{module}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    public ResponseEntity<List<PdfTemplate>> getByModule(@PathVariable String module) {
        return ResponseEntity.ok(documentService.getPdfTemplatesByModule(module));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PdfTemplate> createTemplate(@RequestBody PdfTemplate template) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createPdfTemplate(template));
    }
}
