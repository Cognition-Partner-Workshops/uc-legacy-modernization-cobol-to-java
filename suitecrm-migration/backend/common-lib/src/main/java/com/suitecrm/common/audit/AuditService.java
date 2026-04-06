package com.suitecrm.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logChange(UUID entityId, String entityType, String fieldName,
                          String beforeValue, String afterValue, UUID changedBy) {
        AuditLog log = AuditLog.builder()
                .entityId(entityId)
                .entityType(entityType)
                .fieldName(fieldName)
                .beforeValue(beforeValue)
                .afterValue(afterValue)
                .changedBy(changedBy)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAuditTrail(UUID entityId, String entityType) {
        return auditLogRepository.findByEntityIdAndEntityTypeOrderByChangedAtDesc(entityId, entityType);
    }
}
