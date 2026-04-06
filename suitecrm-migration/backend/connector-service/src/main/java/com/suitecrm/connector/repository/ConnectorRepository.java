package com.suitecrm.connector.repository;

import com.suitecrm.connector.entity.Connector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConnectorRepository extends JpaRepository<Connector, UUID> {

    Page<Connector> findByDeletedFalse(Pageable pageable);

    List<Connector> findByIsEnabledTrueAndDeletedFalse();

    List<Connector> findByConnectorTypeAndDeletedFalse(String connectorType);

    List<Connector> findByStatusAndDeletedFalse(String status);
}
