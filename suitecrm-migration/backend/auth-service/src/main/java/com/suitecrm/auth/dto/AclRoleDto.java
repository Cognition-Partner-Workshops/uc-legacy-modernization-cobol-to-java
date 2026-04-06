package com.suitecrm.auth.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AclRoleDto {

    private UUID id;
    private String name;
    private String description;
    private Boolean isAdmin;
    private List<AclActionDto> actions;
    private LocalDateTime dateEntered;
    private LocalDateTime dateModified;
}
