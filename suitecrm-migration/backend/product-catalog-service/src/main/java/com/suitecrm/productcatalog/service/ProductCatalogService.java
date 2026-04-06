package com.suitecrm.productcatalog.service;

import com.suitecrm.productcatalog.dto.*;
import com.suitecrm.productcatalog.entity.*;
import com.suitecrm.productcatalog.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductCatalogService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductTypeRepository typeRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ShipperRepository shipperRepository;
    private final ProductBundleRepository bundleRepository;

    // Products
    public Page<ProductDto> listProducts(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable).map(this::toProductDto);
    }

    public Page<ProductDto> listProductsByCategory(UUID categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndDeletedFalse(categoryId, pageable).map(this::toProductDto);
    }

    public ProductDto getProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return toProductDto(product);
    }

    public ProductDto createProduct(ProductCreateRequest request, UUID userId) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus())
                .partNumber(request.getPartNumber())
                .costPrice(request.getCostPrice())
                .listPrice(request.getListPrice())
                .discountPrice(request.getDiscountPrice())
                .categoryId(request.getCategoryId())
                .productTypeId(request.getProductTypeId())
                .manufacturerId(request.getManufacturerId())
                .weight(request.getWeight())
                .qtyInStock(request.getQtyInStock())
                .taxClass(request.getTaxClass())
                .website(request.getWebsite())
                .mftPartNum(request.getMftPartNum())
                .vendorPartNum(request.getVendorPartNum())
                .assignedUserId(userId)
                .createdBy(userId)
                .build();
        return toProductDto(productRepository.save(product));
    }

    public ProductDto updateProduct(UUID id, ProductCreateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getPartNumber() != null) product.setPartNumber(request.getPartNumber());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getListPrice() != null) product.setListPrice(request.getListPrice());
        if (request.getDiscountPrice() != null) product.setDiscountPrice(request.getDiscountPrice());
        if (request.getCategoryId() != null) product.setCategoryId(request.getCategoryId());
        if (request.getQtyInStock() != null) product.setQtyInStock(request.getQtyInStock());
        return toProductDto(productRepository.save(product));
    }

    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        product.setDeleted(true);
        productRepository.save(product);
    }

    public Page<ProductDto> searchProducts(String query, Pageable pageable) {
        return productRepository.search(query, pageable).map(this::toProductDto);
    }

    public List<ProductDto> getLowStockProducts(int threshold) {
        return productRepository.findLowStock(threshold).stream()
                .map(this::toProductDto).collect(Collectors.toList());
    }

    // Categories
    public List<ProductCategoryDto> listCategories() {
        return categoryRepository.findByParentIdIsNullAndDeletedFalse().stream()
                .map(this::toCategoryDto).collect(Collectors.toList());
    }

    public ProductCategoryDto createCategory(ProductCategoryDto request) {
        ProductCategory category = ProductCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .listOrder(request.getListOrder())
                .build();
        return toCategoryDto(categoryRepository.save(category));
    }

    // Types
    public List<ProductType> listTypes() {
        return typeRepository.findByDeletedFalse();
    }

    // Manufacturers
    public List<Manufacturer> listManufacturers() {
        return manufacturerRepository.findByDeletedFalse();
    }

    // Shippers
    public List<Shipper> listShippers() {
        return shipperRepository.findByDeletedFalse();
    }

    // Mappers
    private ProductDto toProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .status(product.getStatus())
                .partNumber(product.getPartNumber())
                .costPrice(product.getCostPrice())
                .listPrice(product.getListPrice())
                .discountPrice(product.getDiscountPrice())
                .categoryId(product.getCategoryId())
                .productTypeId(product.getProductTypeId())
                .manufacturerId(product.getManufacturerId())
                .weight(product.getWeight())
                .qtyInStock(product.getQtyInStock())
                .taxClass(product.getTaxClass())
                .website(product.getWebsite())
                .mftPartNum(product.getMftPartNum())
                .vendorPartNum(product.getVendorPartNum())
                .assignedUserId(product.getAssignedUserId())
                .dateEntered(product.getDateEntered())
                .dateModified(product.getDateModified())
                .build();
    }

    private ProductCategoryDto toCategoryDto(ProductCategory category) {
        ProductCategoryDto dto = ProductCategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .listOrder(category.getListOrder())
                .build();
        List<ProductCategory> children = categoryRepository.findByParentIdAndDeletedFalse(category.getId());
        if (!children.isEmpty()) {
            dto.setChildren(children.stream().map(this::toCategoryDto).collect(Collectors.toList()));
        }
        return dto;
    }
}
