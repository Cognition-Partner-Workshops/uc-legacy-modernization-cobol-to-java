package com.suitecrm.template.controller;

import com.suitecrm.template.dto.*;
import com.suitecrm.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/api/templates") @RequiredArgsConstructor
public class TemplateController {
    private final TemplateService templateService;

    @GetMapping("/email")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_MARKETING')")
    public ResponseEntity<Page<EmailTemplateDto>> getEmailTemplates(Pageable pageable) { return ResponseEntity.ok(templateService.getAllEmailTemplates(pageable)); }

    @GetMapping("/email/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER','ROLE_MARKETING')")
    public ResponseEntity<EmailTemplateDto> getEmailTemplateById(@PathVariable UUID id) { return ResponseEntity.ok(templateService.getEmailTemplateById(id)); }

    @GetMapping("/pdf")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER')")
    public ResponseEntity<Page<PdfTemplateDto>> getPdfTemplates(Pageable pageable) { return ResponseEntity.ok(templateService.getAllPdfTemplates(pageable)); }

    @GetMapping("/pdf/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SALES_MANAGER')")
    public ResponseEntity<PdfTemplateDto> getPdfTemplateById(@PathVariable UUID id) { return ResponseEntity.ok(templateService.getPdfTemplateById(id)); }
}
