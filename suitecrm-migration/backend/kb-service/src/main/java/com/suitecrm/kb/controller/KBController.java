package com.suitecrm.kb.controller;

import com.suitecrm.kb.dto.*;
import com.suitecrm.kb.service.KBService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kb")
@RequiredArgsConstructor
public class KBController {

    private final KBService kbService;

    // Articles
    @GetMapping("/articles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<KBContentDto>> listArticles(Pageable pageable) {
        return ResponseEntity.ok(kbService.listArticles(pageable));
    }

    @GetMapping("/articles/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<KBContentDto>> listByStatus(
            @PathVariable String status, Pageable pageable) {
        return ResponseEntity.ok(kbService.listArticlesByStatus(status, pageable));
    }

    @GetMapping("/articles/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<KBContentDto>> listByCategory(
            @PathVariable UUID categoryId, Pageable pageable) {
        return ResponseEntity.ok(kbService.listArticlesByCategory(categoryId, pageable));
    }

    @GetMapping("/articles/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<KBContentDto> getArticle(@PathVariable UUID id) {
        return ResponseEntity.ok(kbService.getArticle(id));
    }

    @PostMapping("/articles")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<KBContentDto> createArticle(
            @Valid @RequestBody KBContentCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(kbService.createArticle(request, userId));
    }

    @PutMapping("/articles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<KBContentDto> updateArticle(
            @PathVariable UUID id, @Valid @RequestBody KBContentCreateRequest request) {
        return ResponseEntity.ok(kbService.updateArticle(id, request));
    }

    @DeleteMapping("/articles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteArticle(@PathVariable UUID id) {
        kbService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/articles/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<KBContentDto>> searchArticles(
            @RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(kbService.searchArticles(query, pageable));
    }

    @GetMapping("/articles/most-viewed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KBContentDto>> getMostViewed(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(kbService.getMostViewed(limit));
    }

    @PostMapping("/articles/{id}/rate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<KBContentDto> rateArticle(
            @PathVariable UUID id, @RequestParam boolean helpful) {
        return ResponseEntity.ok(kbService.rateArticle(id, helpful));
    }

    @GetMapping("/articles/{id}/documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KBDocumentDto>> getArticleDocuments(@PathVariable UUID id) {
        return ResponseEntity.ok(kbService.getDocumentsByArticle(id));
    }

    // Categories
    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<KBCategoryDto>> listCategories() {
        return ResponseEntity.ok(kbService.listCategories());
    }

    @GetMapping("/categories/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<KBCategoryDto> getCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(kbService.getCategory(id));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<KBCategoryDto> createCategory(
            @Valid @RequestBody KBCategoryDto request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(kbService.createCategory(request, userId));
    }

    // Tags
    @GetMapping("/tags")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> listTags() {
        return ResponseEntity.ok(kbService.listTags());
    }

    @GetMapping("/tags/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> searchTags(@RequestParam String query) {
        return ResponseEntity.ok(kbService.searchTags(query));
    }
}
