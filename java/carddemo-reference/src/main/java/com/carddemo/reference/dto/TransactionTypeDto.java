package com.carddemo.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Request/response payload for a transaction type.
 *
 * <p>Validation mirrors the field rules enforced by the legacy online editor
 * {@code COTRTUPC} (type code is exactly 2 characters; description must not be
 * blank and fits the {@code TR_DESCRIPTION VARCHAR(50)} column).</p>
 */
public class TransactionTypeDto {

    @NotBlank(message = "Transaction type code is required")
    @Size(min = 2, max = 2, message = "Transaction type code must be exactly 2 characters")
    private String typeCode;

    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must be at most 50 characters")
    private String description;

    /** Populated only on the single-type read; categories owned by this type. */
    private List<TransactionCategoryDto> categories = new ArrayList<>();

    public TransactionTypeDto() {
    }

    public TransactionTypeDto(String typeCode, String description) {
        this.typeCode = typeCode;
        this.description = description;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TransactionCategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<TransactionCategoryDto> categories) {
        this.categories = categories;
    }
}
