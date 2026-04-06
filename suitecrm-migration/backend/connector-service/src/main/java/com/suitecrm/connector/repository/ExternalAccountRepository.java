package com.suitecrm.connector.repository;

import com.suitecrm.connector.entity.ExternalAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExternalAccountRepository extends JpaRepository<ExternalAccount, UUID> {

    List<ExternalAccount> findByConnectorIdAndDeletedFalse(UUID connectorId);

    List<ExternalAccount> findByUserIdAndDeletedFalse(UUID userId);

    List<ExternalAccount> findByApplicationAndDeletedFalse(String application);
}
