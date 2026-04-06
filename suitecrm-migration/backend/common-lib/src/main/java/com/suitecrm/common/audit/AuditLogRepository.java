package com.suitecrm.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByEntityIdAndEntityTypeOrderByChangedAtDesc(UUID entityId, String entityType);
    Page<AuditLog> findByEntityTypeOrderByChangedAtDesc(String entityType, Pageable pageable);
    List<AuditLog> findByChangedByOrderByChangedAtDesc(UUID changedBy);
}
