package com.suitecrm.productcatalog.controller;

import com.suitecrm.productcatalog.dto.*;
import com.suitecrm.productcatalog.entity.*;
import com.suitecrm.productcatalog.service.ProductCatalogService;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductCatalogController {

    private final ProductCatalogService catalogService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ProductDto>> listProducts(Pageable pageable) {
        return ResponseEntity.ok(catalogService.listProducts(pageable));
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ProductDto>> listByCategory(@PathVariable UUID categoryId, Pageable pageable) {
        return ResponseEntity.ok(catalogService.listProductsByCategory(categoryId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductDto> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(catalogService.getProduct(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ProductDto> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createProduct(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID id, @Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.ok(catalogService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        catalogService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ProductDto>> search(@RequestParam String q, Pageable pageable) {
        return ResponseEntity.ok(catalogService.searchProducts(q, pageable));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ProductDto>> getLowStock(@RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(catalogService.getLowStockProducts(threshold));
    }

    // Categories
    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductCategoryDto>> listCategories() {
        return ResponseEntity.ok(catalogService.listCategories());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProductCategoryDto> createCategory(@RequestBody ProductCategoryDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createCategory(request));
    }

    // Types
    @GetMapping("/types")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductType>> listTypes() {
        return ResponseEntity.ok(catalogService.listTypes());
    }

    // Manufacturers
    @GetMapping("/manufacturers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Manufacturer>> listManufacturers() {
        return ResponseEntity.ok(catalogService.listManufacturers());
    }

    // Shippers
    @GetMapping("/shippers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Shipper>> listShippers() {
        return ResponseEntity.ok(catalogService.listShippers());
    }
}
