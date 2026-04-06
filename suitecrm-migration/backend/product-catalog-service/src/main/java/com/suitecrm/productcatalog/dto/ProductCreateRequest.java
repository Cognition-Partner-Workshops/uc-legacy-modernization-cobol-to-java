package com.suitecrm.productcatalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    @NotBlank(message = "Product name is required")
    private String name;
    private String description;
    private String status;
    private String partNumber;
    private BigDecimal costPrice;
    private BigDecimal listPrice;
    private BigDecimal discountPrice;
    private UUID categoryId;
    private UUID productTypeId;
    private UUID manufacturerId;
    private BigDecimal weight;
    private Integer qtyInStock;
    private String taxClass;
    private String website;
    private String mftPartNum;
    private String vendorPartNum;
}
