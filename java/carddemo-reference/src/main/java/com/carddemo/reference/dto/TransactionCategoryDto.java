package com.carddemo.reference.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request/response payload for a transaction category.
 *
 * <p>Mirrors {@code app/cpy/CVTRA04Y.cpy}: a 2-character type code, a
 * 4-character category code, and a description backed by
 * {@code TRC_CAT_DATA VARCHAR(50)}.</p>
 */
public class TransactionCategoryDto {

    /** Owning type code; normally taken from the URL path on writes. */
    @Size(min = 2, max = 2, message = "Transaction type code must be exactly 2 characters")
    private String typeCode;

    @NotBlank(message = "Category code is required")
    @Size(min = 4, max = 4, message = "Category code must be exactly 4 characters")
    private String categoryCode;

    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must be at most 50 characters")
    private String description;

    public TransactionCategoryDto() {
    }

    public TransactionCategoryDto(String typeCode, String categoryCode, String description) {
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
        this.description = description;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
