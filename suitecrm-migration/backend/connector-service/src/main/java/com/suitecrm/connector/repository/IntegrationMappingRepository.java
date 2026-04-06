package com.suitecrm.connector.repository;

import com.suitecrm.connector.entity.IntegrationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntegrationMappingRepository extends JpaRepository<IntegrationMapping, UUID> {

    List<IntegrationMapping> findByConnectorIdAndDeletedFalse(UUID connectorId);

    List<IntegrationMapping> findByConnectorIdAndSourceModuleAndDeletedFalse(UUID connectorId, String sourceModule);
}
