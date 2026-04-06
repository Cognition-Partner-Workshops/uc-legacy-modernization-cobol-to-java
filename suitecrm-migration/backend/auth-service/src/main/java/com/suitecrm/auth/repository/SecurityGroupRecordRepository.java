package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.SecurityGroupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityGroupRecordRepository extends JpaRepository<SecurityGroupRecord, UUID> {
    List<SecurityGroupRecord> findByRecordIdAndModuleAndDeletedFalse(UUID recordId, String module);
    List<SecurityGroupRecord> findBySecuritygroupIdAndDeletedFalse(UUID securitygroupId);
}
