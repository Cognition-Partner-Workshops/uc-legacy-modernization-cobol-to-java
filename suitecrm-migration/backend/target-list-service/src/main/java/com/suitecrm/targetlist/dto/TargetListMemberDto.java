package com.suitecrm.targetlist.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetListMemberDto {
    private UUID id;
    private UUID prospectListId;
    private UUID relatedId;
    private String relatedType;
}
