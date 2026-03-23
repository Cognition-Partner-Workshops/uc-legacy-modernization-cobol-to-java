package com.carddemo.dto;

import java.time.LocalDate;

public class CardUpdateRequest {

    private String embossedName;
    private LocalDate expirationDate;
    private String activeStatus;

    public CardUpdateRequest() {}

    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
