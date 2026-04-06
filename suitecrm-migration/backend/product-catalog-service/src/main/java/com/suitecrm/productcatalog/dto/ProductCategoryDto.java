package com.suitecrm.productcatalog.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDto {
    private UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private Integer listOrder;
    private List<ProductCategoryDto> children;
}
