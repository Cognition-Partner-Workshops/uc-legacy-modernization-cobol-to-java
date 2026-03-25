package com.carddemo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for card update - replaces COCRDUPC's validation logic.
 */
public class CardUpdateDTO {

    @Size(max = 50, message = "Embossed name must be at most 50 characters")
    private String embossedName;

    @Pattern(regexp = "[YN]", message = "Active status must be 'Y' or 'N'")
    private String activeStatus;

    private String expirationDate;

    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
}
