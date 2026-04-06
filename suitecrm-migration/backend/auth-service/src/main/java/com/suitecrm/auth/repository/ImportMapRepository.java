package com.suitecrm.auth.repository;

import com.suitecrm.auth.entity.ImportMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ImportMapRepository extends JpaRepository<ImportMap, UUID> {
    List<ImportMap> findByModuleAndDeletedFalse(String module);
    List<ImportMap> findByAssignedUserIdAndDeletedFalse(UUID userId);
}
