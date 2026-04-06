package com.suitecrm.productcatalog.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private UUID id;
    private String name;
    private String description;
    private String status;
    private String partNumber;
    private BigDecimal costPrice;
    private BigDecimal listPrice;
    private BigDecimal discountPrice;
    private UUID categoryId;
    private String categoryName;
    private UUID productTypeId;
    private UUID manufacturerId;
    private String manufacturerName;
    private BigDecimal weight;
    private Integer qtyInStock;
    private String taxClass;
    private String website;
    private String mftPartNum;
    private String vendorPartNum;
    private UUID assignedUserId;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
