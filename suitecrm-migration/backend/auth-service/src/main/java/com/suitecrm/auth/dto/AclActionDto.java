package com.suitecrm.auth.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AclActionDto {

    private UUID id;
    private String name;
    private String category;
    private String acltype;
    private Integer aclaccess;
}
