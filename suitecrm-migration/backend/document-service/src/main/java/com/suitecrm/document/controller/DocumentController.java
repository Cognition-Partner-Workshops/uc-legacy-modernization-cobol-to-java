package com.suitecrm.document.controller;

import com.suitecrm.document.entity.Document;
import com.suitecrm.document.repository.DocumentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Page<Document>> listDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateEntered") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Document> documents;
        if (search != null && !search.isBlank()) {
            documents = documentRepository.searchDocuments(search, pageable);
        } else {
            documents = documentRepository.findByDeletedFalse(pageable);
        }
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER','VIEWER')")
    public ResponseEntity<Document> getDocument(@PathVariable UUID id) {
        return ResponseEntity.ok(documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Document not found")));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER')")
    public ResponseEntity<Document> createDocument(@Valid @RequestBody Document document) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentRepository.save(document));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES','SUPPORT','MARKETING','USER')")
    public ResponseEntity<Document> updateDocument(@PathVariable UUID id, @Valid @RequestBody Document update) {
        Document existing = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        existing.setDocumentName(update.getDocumentName());
        existing.setCategoryId(update.getCategoryId());
        existing.setSubcategoryId(update.getSubcategoryId());
        existing.setStatusId(update.getStatusId());
        existing.setDescription(update.getDescription());
        return ResponseEntity.ok(documentRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        Document document = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        document.setDeleted(true);
        documentRepository.save(document);
        return ResponseEntity.noContent().build();
    }
}
