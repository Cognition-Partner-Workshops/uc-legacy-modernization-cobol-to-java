package com.suitecrm.kb.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KBCategoryDto {
    private UUID id;
    private String name;
    private String description;
    private UUID parentId;
    private Integer displayOrder;
    private Boolean isExternal;
    private List<KBCategoryDto> children;
    private Integer articleCount;
}
