package com.carddemo.reference.service;

import com.carddemo.reference.dto.BatchOperationDto;
import com.carddemo.reference.dto.TransactionCategoryDto;
import com.carddemo.reference.dto.TransactionTypeDto;
import com.carddemo.reference.exception.DuplicateResourceException;
import com.carddemo.reference.exception.ReferentialIntegrityException;
import com.carddemo.reference.exception.ResourceNotFoundException;
import com.carddemo.reference.model.TransactionType;
import com.carddemo.reference.model.TransactionTypeCategory;
import com.carddemo.reference.model.TransactionTypeCategoryId;
import com.carddemo.reference.repository.TransactionTypeCategoryRepository;
import com.carddemo.reference.repository.TransactionTypeRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Business logic for transaction type and category reference data.
 *
 * <p>Consolidates the behaviour of the legacy COBOL programs:</p>
 * <ul>
 *   <li>{@code COTRTLIC} &ndash; list/search, single read and delete.</li>
 *   <li>{@code COTRTUPC} &ndash; create and update with field validation.</li>
 *   <li>{@code COBTUPDT} &ndash; transactional batch INSERT/UPDATE/DELETE.</li>
 * </ul>
 */
@Service
@Transactional
public class TransactionTypeService {

    private final TransactionTypeRepository typeRepository;
    private final TransactionTypeCategoryRepository categoryRepository;

    public TransactionTypeService(TransactionTypeRepository typeRepository,
                                  TransactionTypeCategoryRepository categoryRepository) {
        this.typeRepository = typeRepository;
        this.categoryRepository = categoryRepository;
    }

    // ----- Transaction Types (COTRTLIC list, COTRTUPC add/edit) -----

    /** Paginated list with optional search, replacing the COTRTLIC browse. */
    @Transactional(readOnly = true)
    public Page<TransactionTypeDto> listTypes(String search, Pageable pageable) {
        Page<TransactionType> page;
        if (StringUtils.hasText(search)) {
            page = typeRepository
                    .findByTypeCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            search, search, pageable);
        } else {
            page = typeRepository.findAll(pageable);
        }
        return page.map(this::toTypeDto);
    }

    /** Single type with its categories (COTRTLIC select / COTRTUPC edit load). */
    @Transactional(readOnly = true)
    public TransactionTypeDto getType(String typeCode) {
        TransactionType type = typeRepository.findById(typeCode)
                .orElseThrow(() -> notFoundType(typeCode));
        TransactionTypeDto dto = toTypeDto(type);
        dto.setCategories(categoryRepository
                .findByIdTypeCodeOrderByIdCategoryCode(typeCode)
                .stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList()));
        return dto;
    }

    /** Create a new transaction type (COTRTUPC INSERT). */
    public TransactionTypeDto createType(TransactionTypeDto dto) {
        String code = normalizeTypeCode(dto.getTypeCode());
        if (typeRepository.existsById(code)) {
            throw new DuplicateResourceException(
                    "Transaction type '" + code + "' already exists");
        }
        TransactionType saved = typeRepository.save(
                new TransactionType(code, dto.getDescription().trim()));
        return toTypeDto(saved);
    }

    /** Update the description of an existing type (COTRTUPC UPDATE). */
    public TransactionTypeDto updateType(String typeCode, TransactionTypeDto dto) {
        TransactionType type = typeRepository.findById(typeCode)
                .orElseThrow(() -> notFoundType(typeCode));
        type.setDescription(dto.getDescription().trim());
        return toTypeDto(typeRepository.save(type));
    }

    /**
     * Delete a transaction type (COTRTLIC delete). Fails with
     * {@link ReferentialIntegrityException} if categories exist, mirroring the
     * DB2 {@code ON DELETE RESTRICT} foreign key.
     */
    public void deleteType(String typeCode) {
        TransactionType type = typeRepository.findById(typeCode)
                .orElseThrow(() -> notFoundType(typeCode));
        if (categoryRepository.existsByIdTypeCode(typeCode)) {
            throw new ReferentialIntegrityException(
                    "Cannot delete transaction type '" + typeCode
                            + "' because it has associated categories");
        }
        typeRepository.delete(type);
    }

    // ----- Transaction Categories -----

    @Transactional(readOnly = true)
    public List<TransactionCategoryDto> listCategories(String typeCode) {
        requireTypeExists(typeCode);
        return categoryRepository.findByIdTypeCodeOrderByIdCategoryCode(typeCode)
                .stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }

    public TransactionCategoryDto createCategory(String typeCode, TransactionCategoryDto dto) {
        requireTypeExists(typeCode);
        TransactionTypeCategoryId id =
                new TransactionTypeCategoryId(typeCode, dto.getCategoryCode());
        if (categoryRepository.existsById(id)) {
            throw new DuplicateResourceException(
                    "Category '" + dto.getCategoryCode() + "' already exists for type '"
                            + typeCode + "'");
        }
        TransactionTypeCategory saved = categoryRepository.save(
                new TransactionTypeCategory(id, dto.getDescription().trim()));
        return toCategoryDto(saved);
    }

    public TransactionCategoryDto updateCategory(String typeCode, String categoryCode,
                                                 TransactionCategoryDto dto) {
        TransactionTypeCategoryId id =
                new TransactionTypeCategoryId(typeCode, categoryCode);
        TransactionTypeCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> notFoundCategory(typeCode, categoryCode));
        category.setDescription(dto.getDescription().trim());
        return toCategoryDto(categoryRepository.save(category));
    }

    public void deleteCategory(String typeCode, String categoryCode) {
        TransactionTypeCategoryId id =
                new TransactionTypeCategoryId(typeCode, categoryCode);
        TransactionTypeCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> notFoundCategory(typeCode, categoryCode));
        categoryRepository.delete(category);
    }

    // ----- Batch maintenance (COBTUPDT) -----

    /**
     * Apply a list of maintenance operations in a single transaction. Any
     * failure rolls back the entire batch, matching the all-or-nothing nature
     * of the original {@code COBTUPDT} job.
     */
    public void processBatch(List<BatchOperationDto> operations) {
        for (BatchOperationDto op : operations) {
            String code = normalizeTypeCode(op.getTypeCode());
            switch (op.getAction()) {
                case INSERT:
                    if (typeRepository.existsById(code)) {
                        throw new DuplicateResourceException(
                                "Transaction type '" + code + "' already exists");
                    }
                    typeRepository.save(
                            new TransactionType(code, requireDescription(op.getDescription())));
                    break;
                case UPDATE:
                    TransactionType existing = typeRepository.findById(code)
                            .orElseThrow(() -> notFoundType(code));
                    existing.setDescription(requireDescription(op.getDescription()));
                    typeRepository.save(existing);
                    break;
                case DELETE:
                    TransactionType toDelete = typeRepository.findById(code)
                            .orElseThrow(() -> notFoundType(code));
                    if (categoryRepository.existsByIdTypeCode(code)) {
                        throw new ReferentialIntegrityException(
                                "Cannot delete transaction type '" + code
                                        + "' because it has associated categories");
                    }
                    typeRepository.delete(toDelete);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported action: " + op.getAction());
            }
        }
    }

    // ----- Helpers -----

    private void requireTypeExists(String typeCode) {
        if (!typeRepository.existsById(typeCode)) {
            throw notFoundType(typeCode);
        }
    }

    private String requireDescription(String description) {
        if (!StringUtils.hasText(description)) {
            throw new IllegalArgumentException("Description is required for INSERT/UPDATE");
        }
        return description.trim();
    }

    private String normalizeTypeCode(String typeCode) {
        return typeCode == null ? null : typeCode.trim();
    }

    private ResourceNotFoundException notFoundType(String typeCode) {
        return new ResourceNotFoundException("Transaction type '" + typeCode + "' not found");
    }

    private ResourceNotFoundException notFoundCategory(String typeCode, String categoryCode) {
        return new ResourceNotFoundException("Category '" + categoryCode
                + "' not found for transaction type '" + typeCode + "'");
    }

    private TransactionTypeDto toTypeDto(TransactionType type) {
        return new TransactionTypeDto(type.getTypeCode(), type.getDescription());
    }

    private TransactionCategoryDto toCategoryDto(TransactionTypeCategory category) {
        return new TransactionCategoryDto(
                category.getId().getTypeCode(),
                category.getId().getCategoryCode(),
                category.getDescription());
    }
}
