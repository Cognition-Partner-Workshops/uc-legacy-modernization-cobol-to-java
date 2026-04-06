package com.suitecrm.document.service;

import com.suitecrm.document.dto.*;
import com.suitecrm.document.entity.Document;
import com.suitecrm.document.entity.DocumentRevision;
import com.suitecrm.document.entity.EmailTemplate;
import com.suitecrm.document.entity.PdfTemplate;
import com.suitecrm.document.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository documentRevisionRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final PdfTemplateRepository pdfTemplateRepository;

    // === Documents ===
    @Transactional(readOnly = true)
    public Page<DocumentDto> listDocuments(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Document> docs;
        if (search != null && !search.isBlank()) {
            docs = documentRepository.searchDocuments(search, pageable);
        } else {
            docs = documentRepository.findByDeletedFalse(pageable);
        }
        return docs.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocument(UUID id) {
        Document doc = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        return toDto(doc);
    }

    public DocumentDto createDocument(DocumentCreateRequest request, UUID createdBy) {
        log.info("Creating document: name={}", request.getName());
        Document doc = Document.builder()
                .name(request.getName())
                .documentType(request.getDocumentType())
                .status(request.getStatus() != null ? request.getStatus() : "Active")
                .categoryId(request.getCategoryId())
                .subcategoryId(request.getSubcategoryId())
                .description(request.getDescription())
                .activeDate(request.getActiveDate())
                .expirationDate(request.getExpirationDate())
                .assignedUserId(request.getAssignedUserId())
                .createdBy(createdBy)
                .build();
        Document saved = documentRepository.save(doc);
        log.info("Created document: id={}", saved.getId());
        return toDto(saved);
    }

    public void deleteDocument(UUID id) {
        Document doc = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
        doc.setDeleted(true);
        documentRepository.save(doc);
    }

    // === Document Revisions ===
    @Transactional(readOnly = true)
    public List<DocumentRevision> getDocumentRevisions(UUID documentId) {
        return documentRevisionRepository.findByDocumentIdAndDeletedFalseOrderByRevisionDesc(documentId);
    }

    public DocumentRevision createRevision(UUID documentId, String filename, String fileExt, String fileMimeType, UUID createdBy) {
        int nextRevision = documentRevisionRepository.findByDocumentIdAndDeletedFalseOrderByRevisionDesc(documentId).size() + 1;
        DocumentRevision revision = DocumentRevision.builder()
                .documentId(documentId)
                .filename(filename)
                .fileExt(fileExt)
                .fileMimeType(fileMimeType)
                .revision(nextRevision)
                .createdBy(createdBy)
                .build();
        return documentRevisionRepository.save(revision);
    }

    // === Email Templates ===
    @Transactional(readOnly = true)
    public Page<EmailTemplate> listEmailTemplates(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        if (search != null && !search.isBlank()) {
            return emailTemplateRepository.searchTemplates(search, pageable);
        }
        return emailTemplateRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public EmailTemplate getEmailTemplate(UUID id) {
        return emailTemplateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Email template not found with id: " + id));
    }

    public EmailTemplate createEmailTemplate(EmailTemplate template) {
        return emailTemplateRepository.save(template);
    }

    // === PDF Templates ===
    @Transactional(readOnly = true)
    public Page<PdfTemplate> listPdfTemplates(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return pdfTemplateRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public PdfTemplate getPdfTemplate(UUID id) {
        return pdfTemplateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("PDF template not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<PdfTemplate> getPdfTemplatesByModule(String module) {
        return pdfTemplateRepository.findByModuleAndDeletedFalse(module);
    }

    public PdfTemplate createPdfTemplate(PdfTemplate template) {
        return pdfTemplateRepository.save(template);
    }

    private DocumentDto toDto(Document entity) {
        return DocumentDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .documentType(entity.getDocumentType())
                .status(entity.getStatus())
                .categoryId(entity.getCategoryId())
                .subcategoryId(entity.getSubcategoryId())
                .description(entity.getDescription())
                .activeDate(entity.getActiveDate())
                .expirationDate(entity.getExpirationDate())
                .assignedUserId(entity.getAssignedUserId())
                .createdBy(entity.getCreatedBy())
                .dateEntered(entity.getDateEntered())
                .dateModified(entity.getDateModified())
                .build();
    }
}
