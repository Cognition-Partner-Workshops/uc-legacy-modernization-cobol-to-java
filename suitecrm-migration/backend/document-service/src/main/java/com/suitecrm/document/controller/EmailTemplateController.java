package com.suitecrm.document.controller;

import com.suitecrm.document.entity.EmailTemplate;
import com.suitecrm.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/email-templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final DocumentService documentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MARKETING','USER')")
    public ResponseEntity<Page<EmailTemplate>> listTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(documentService.listEmailTemplates(page, size, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MARKETING','USER')")
    public ResponseEntity<EmailTemplate> getTemplate(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getEmailTemplate(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','MARKETING')")
    public ResponseEntity<EmailTemplate> createTemplate(@RequestBody EmailTemplate template) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentService.createEmailTemplate(template));
    }
}
