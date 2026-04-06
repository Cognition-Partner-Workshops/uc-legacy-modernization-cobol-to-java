package com.suitecrm.connector.repository;

import com.suitecrm.connector.entity.ConnectorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConnectorLogRepository extends JpaRepository<ConnectorLog, UUID> {

    Page<ConnectorLog> findByConnectorIdOrderByDateEnteredDesc(UUID connectorId, Pageable pageable);

    Page<ConnectorLog> findByConnectorIdAndStatusOrderByDateEnteredDesc(UUID connectorId, String status, Pageable pageable);
}
