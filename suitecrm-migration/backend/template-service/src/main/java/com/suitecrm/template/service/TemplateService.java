package com.suitecrm.template.service;

import com.suitecrm.template.dto.*;
import com.suitecrm.template.entity.*;
import com.suitecrm.template.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class TemplateService {
    private final EmailTemplateRepository emailTemplateRepository;
    private final PdfTemplateRepository pdfTemplateRepository;

    public Page<EmailTemplateDto> getAllEmailTemplates(Pageable pageable) {
        return emailTemplateRepository.findByDeletedFalse(pageable).map(this::toEmailTemplateDto);
    }
    public EmailTemplateDto getEmailTemplateById(UUID id) {
        return toEmailTemplateDto(emailTemplateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Email template not found: " + id)));
    }
    public Page<PdfTemplateDto> getAllPdfTemplates(Pageable pageable) {
        return pdfTemplateRepository.findByDeletedFalse(pageable).map(this::toPdfTemplateDto);
    }
    public PdfTemplateDto getPdfTemplateById(UUID id) {
        return toPdfTemplateDto(pdfTemplateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("PDF template not found: " + id)));
    }

    private EmailTemplateDto toEmailTemplateDto(EmailTemplate t) {
        return EmailTemplateDto.builder().id(t.getId()).name(t.getName()).subject(t.getSubject())
                .body(t.getBody()).bodyHtml(t.getBodyHtml()).type(t.getType()).textOnly(t.getTextOnly())
                .description(t.getDescription()).assignedUserId(t.getAssignedUserId())
                .dateEntered(t.getDateEntered()).dateModified(t.getDateModified()).build();
    }
    private PdfTemplateDto toPdfTemplateDto(PdfTemplate t) {
        return PdfTemplateDto.builder().id(t.getId()).name(t.getName()).type(t.getType())
                .moduleName(t.getModuleName()).body(t.getBody()).pdfHeader(t.getPdfHeader())
                .pdfFooter(t.getPdfFooter()).pageSize(t.getPageSize()).orientation(t.getOrientation())
                .description(t.getDescription()).assignedUserId(t.getAssignedUserId())
                .dateEntered(t.getDateEntered()).dateModified(t.getDateModified()).build();
    }
}
