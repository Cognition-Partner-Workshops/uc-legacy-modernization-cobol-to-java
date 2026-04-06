package com.suitecrm.auth.engine;

import com.suitecrm.auth.entity.AuditLog;
import com.suitecrm.auth.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditTrailService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void recordChanges(UUID recordId, String module, Map<String, Object> oldValues,
                              Map<String, Object> newValues, UUID changedBy) {
        Set<String> allFields = new HashSet<>();
        if (oldValues != null) allFields.addAll(oldValues.keySet());
        if (newValues != null) allFields.addAll(newValues.keySet());

        for (String field : allFields) {
            Object oldVal = oldValues != null ? oldValues.get(field) : null;
            Object newVal = newValues != null ? newValues.get(field) : null;

            if (!Objects.equals(oldVal, newVal)) {
                AuditLog entry = AuditLog.builder()
                    .parentId(recordId)
                    .parentModule(module)
                    .fieldName(field)
                    .dataType(newVal != null ? newVal.getClass().getSimpleName() : "String")
                    .beforeValueString(oldVal != null ? String.valueOf(oldVal) : null)
                    .afterValueString(newVal != null ? String.valueOf(newVal) : null)
                    .changedBy(changedBy)
                    .build();
                auditLogRepository.save(entry);
            }
        }
    }

    public List<AuditLog> getAuditTrail(UUID recordId, String module) {
        return auditLogRepository.findByParentIdAndParentModuleOrderByDateCreatedDesc(recordId, module);
    }
}
